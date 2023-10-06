package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientDisplayProgressToast;
import de.thedead2.progression_reloaded.network.packets.ClientSyncLevelsPacket;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerPacket;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.PlayerTeamSynchronizer;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


/**
 * Handles and tracks all known levels, there progress and there corresponding quests. There's only one instance
 * of this class active per world.
 **/
public class LevelManager {

    public static final ProgressionLevel CREATIVE = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("creative_level")
                                    .withName("creative-level")
                                    .build(),
            Rewards.Builder.builder().build(),
            Collections.emptySet()
    );

    private static final Marker MARKER = new MarkerManager.Log4jMarker("LevelManager");

    private static LevelManager instance = null;

    /**
     * Map of all known players and their corresponding level.
     **/
    private final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();

    /**
     * Map of all known levels and their corresponding progress.
     **/
    private final Map<KnownPlayer, Map<ProgressionLevel, LevelProgress>> levelProgress = new HashMap<>();

    private final List<ProgressionLevel> levelOrder = new LinkedList<>(ModRegistries.LEVELS.get().getValues().stream().filter(progressionLevel -> !progressionLevel.equals(CREATIVE)).toList());

    private final Map<KnownPlayer, ProgressionLevel> levelCache = new HashMap<>();

    private final QuestManager questManager;

    private final LevelProgressData levelProgressData;


    private LevelManager(DimensionDataStorage dataStorage) {
        instance = this;
        this.levelProgressData = dataStorage.computeIfAbsent(LevelProgressData::load, () -> new LevelProgressData(new HashMap<>(), new HashMap<>(), new HashMap<>()), "levelProgress");
        this.questManager = new QuestManager(dataStorage, this);
        init();
    }


    public static LevelManager create(ServerLevel level) {
        DimensionDataStorage dataStorage = level.getDataStorage();
        return new LevelManager(dataStorage);
    }


    public static LevelManager getInstance() {
        return instance;
    }


    @NotNull
    public ProgressionLevel getPlayerLevel(KnownPlayer player) {
        return this.playerLevels.getOrDefault(player, CREATIVE);
    }


    @Nullable
    public Pair<Player, ProgressionLevel> getHighestPlayerLevel(Collection<? extends Player> players) {
        Triple<Integer, Player, ProgressionLevel> highestLevel = null;

        for(Player player : players) {
            PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
            if(playerData == null) {
                continue;
            }
            ProgressionLevel level = playerData.getProgressionLevel();
            int levelIndex = this.levelOrder.indexOf(level);

            if(highestLevel != null) {
                if(levelIndex > highestLevel.getLeft()) {
                    highestLevel = Triple.of(levelIndex, player, level);
                }
            }
            else {
                highestLevel = Triple.of(levelIndex, player, level);
            }
        }

        if(highestLevel != null) {
            return Pair.of(highestLevel.getMiddle(), highestLevel.getRight());
        }
        else {
            return null;
        }
    }


    public static void onGameModeChange(final PlayerEvent.PlayerChangeGameModeEvent event) {
        if(!ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get()) {
            return;
        }
        GameType newGameMode = event.getNewGameMode();
        GameType currentGameMode = event.getCurrentGameMode();
        if(isGameModeCreativeOrSpectator(newGameMode) && isGameModeNotCreativeAndSpectator(currentGameMode)) {
            LevelManager.getInstance().onCreativeChange(event.getEntity());
        }
        else if(isGameModeNotCreativeAndSpectator(newGameMode) && isGameModeCreativeOrSpectator(currentGameMode)) {
            LevelManager.getInstance().onSurvivalChange(event.getEntity());
        }
    }


    private static boolean isGameModeCreativeOrSpectator(GameType gameMode) {
        return gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR;
    }


    private static boolean isGameModeNotCreativeAndSpectator(GameType gameMode) {
        return gameMode != GameType.CREATIVE && gameMode != GameType.SPECTATOR;
    }


    public void reset() {
        instance = null;
    }


    private void init() {
        loadLevelOrder();
        loadPlayerLevels();
        loadLevelProgress();
        loadLevelCache();
        this.updateStatus();
    }


    private void loadLevelOrder() {
        Collections.sort(this.levelOrder);
    }


    public void updateLevel(PlayerData player, ProgressionLevel nextLevel) {
        KnownPlayer knownPlayer = KnownPlayer.fromSinglePlayer(player);

        if(nextLevel != null && !PREventFactory.onLevelUpdate(nextLevel, player, this.playerLevels.get(knownPlayer))) {
            PlayerTeamSynchronizer.updateProgressionLevel(player, nextLevel);
            this.playerLevels.put(knownPlayer, nextLevel);
            this.syncLevelsWithTeam(knownPlayer, nextLevel);
            this.saveData();
        }
        this.updateStatus();
    }


    private void syncLevelsWithTeam(KnownPlayer player, ProgressionLevel level) {
        PlayerTeam team = PlayerDataHandler.getTeam(player);
        if(team == null) {
            return;
        }
        team.forEachMember(player1 -> this.playerLevels.put(player1, level));
    }


    public void updateLevel(PlayerData player, ResourceLocation nextLevel) {
        if(nextLevel != null) {
            this.updateLevel(player, ModRegistries.LEVELS.get().getValue(nextLevel));
        }
        else {
            this.updateStatus();
        }
    }


    public void updateStatus() {
        Set<KnownPlayer> playersToRemove = new HashSet<>();
        this.playerLevels.forEach((player, level) -> {
            LOGGER.debug(MARKER, "Updating level status for player: {}", player.name());

            if(PlayerDataHandler.playerLastOnlineLongAgo(player)) {
                LOGGER.debug(MARKER, "Marked player {} for removal!", player.name());
                playersToRemove.add(player);
            }

            PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
            LevelProgress progress = this.levelProgress.get(player).get(level);

            if(progress.isDone() && !progress.hasBeenRewarded()) {
                level.rewardPlayer(playerData);
                progress.setRewarded(true);
                ProgressionLevel nextLevel = this.getNextLevel(level);
                LOGGER.debug(MARKER, "Player {} completed level {}", player.name(), level.getId());
                ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(level.getDisplay(), nextLevel != null ? nextLevel.getId() : null), playerData.getServerPlayer());
                this.updateLevel(playerData, nextLevel);
            }
            else {
                this.syncLevelsToClient(player, level);
                PREventFactory.onLevelStatusUpdate(level, playerData, progress);
                questManager.updateStatus(player, true);
            }
        });

        playersToRemove.forEach(player -> {
            LOGGER.debug(MARKER, "Removing data for player {}!", player.name());
            this.playerLevels.remove(player);
            this.levelProgress.remove(player);
            this.levelCache.remove(player);
            questManager.removePlayerData(player);
            this.saveData();
        });
    }
    // Bsp. 25 level
    // current level 24

    @Nullable
    private ProgressionLevel getNextLevel(ProgressionLevel level) {
        int i = this.levelOrder.indexOf(level) + 1; // 24 + 1 = 25
        if(this.levelOrder.size() <= i) { //size = 25
            return null; // Kein weiteres level vorhanden
        }
        else {
            return this.levelOrder.get(i); //TODO: Fix IndexOutOfBounds when revoking level
        }
    }


    @Nullable
    private ProgressionLevel getPreviousLevel(ProgressionLevel level) {
        int i = this.levelOrder.indexOf(level) - 1;
        if(0 > i) {
            return null;
        }
        else {
            return this.levelOrder.get(i); //TODO: Fix IndexOutOfBounds when revoking level
        }
    }


    private void loadLevelCache() {
        this.levelCache.putAll(this.levelProgressData.levelCache);
    }


    public void updateData() {
        this.saveData();
        this.loadPlayerLevels();
        this.loadLevelProgress();
        this.questManager.updateData();
    }


    public void saveData() {
        LOGGER.debug(MARKER, "Saving data!");
        this.levelProgressData.updateLevelProgressData(this.levelProgress);
        this.levelProgressData.updatePlayerLevels(this.playerLevels);
        this.levelProgressData.updateLevelCache(this.levelCache);
        this.questManager.saveData();
    }


    public ProgressionLevel getLevelForQuest(ProgressionQuest quest) {
        ResourceLocation id = quest.getId();
        return this.levelOrder.stream()
                              .filter(level -> level.getQuests().contains(id))
                              .findAny()
                              .orElseThrow(() -> new IllegalArgumentException("Unknown level for quest: " + id));
    }


    private void loadPlayerLevels() {
        this.playerLevels.putAll(this.levelProgressData.playerLevels);
        PlayerDataHandler.allPlayers().forEach(player -> this.playerLevels.putIfAbsent(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel()));
    }


    private void loadLevelProgress() {
        this.levelProgress.putAll(this.levelProgressData.levelProgress);
        PlayerDataHandler.allPlayers().forEach(player -> {
            Map<ProgressionLevel, LevelProgress> levelProgressMap = new HashMap<>();
            KnownPlayer knownPlayer = KnownPlayer.fromSinglePlayer(player);
            ModRegistries.LEVELS.get().getValues().forEach(level -> {
                levelProgressMap.put(level, new LevelProgress(level, knownPlayer));
            });

            this.levelProgress.putIfAbsent(knownPlayer, levelProgressMap);
        });
    }


    public QuestManager getQuestManager() {
        return this.questManager;
    }


    private void syncLevelsToClient(KnownPlayer player, ProgressionLevel level) {
        LOGGER.debug(MARKER, "Attempting to sync level status with client...");
        PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
        if(playerData == null) {
            LOGGER.debug(MARKER, "No client found to sync level status with! Skipping...");
            return;
        }
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerPacket(playerData), playerData.getServerPlayer());
        ModNetworkHandler.sendToPlayer(new ClientSyncLevelsPacket(level, this.levelProgress.get(player)), playerData.getServerPlayer());
    }


    public void revoke(PlayerData player, ProgressionLevel level) {
        if(!PREventFactory.onLevelRevoke(player, level)) {
            this.resetLevelProgress(player, level);
            this.updateLevel(player, level);
        }
    }


    private void resetLevelProgress(PlayerData player, ProgressionLevel level) {
        KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
        LevelProgress progress = this.levelProgress.get(player1).get(level);
        progress.reset();
        ProgressionLevel nextLevel = this.getNextLevel(level);
        if(nextLevel != null) {
            resetLevelProgress(player, nextLevel);
        }
    }


    public void award(PlayerData player, ProgressionLevel level) {
        if(!PREventFactory.onLevelAward(player, level)) {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            LevelProgress levelProgress = this.levelProgress.get(player1).get(level);
            levelProgress.complete();
            ProgressionLevel previousLevel = this.getPreviousLevel(level);
            if(previousLevel != null) {
                award(player, previousLevel);
            }
            this.updateStatus();
        }
    }


    private void onSurvivalChange(Player entity) {
        KnownPlayer player = KnownPlayer.fromPlayer(entity);
        ProgressionLevel level = levelCache.remove(player);
        this.updateLevel(PlayerDataHandler.getActivePlayer(entity), level != null ? level.getId() : ResourceLocationHelper.getOrDefault(ConfigManager.DEFAULT_STARTING_LEVEL.get(), CREATIVE.getId()));
    }


    private void onCreativeChange(Player entity) {
        KnownPlayer player = KnownPlayer.fromPlayer(entity);
        ProgressionLevel currentLevel = playerLevels.get(player);
        levelCache.put(player, currentLevel);
        this.updateLevel(PlayerDataHandler.getActivePlayer(entity), CREATIVE); //TODO: active player was null???
    }


    public void checkForCreativeMode(PlayerData playerData) {
        ServerPlayer player = playerData.getServerPlayer();
        if((player.gameMode.isCreative() || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() && !playerData.hasProgressionLevel(CREATIVE.getId())) {
            this.onCreativeChange(player);
        }
    }


    private static final class LevelProgressData extends SavedData {

        private final Map<KnownPlayer, Map<ProgressionLevel, LevelProgress>> levelProgress;

        private final Map<KnownPlayer, ProgressionLevel> playerLevels;

        private final Map<KnownPlayer, ProgressionLevel> levelCache;


        public LevelProgressData(Map<KnownPlayer, Map<ProgressionLevel, LevelProgress>> levelProgress, Map<KnownPlayer, ProgressionLevel> playerLevels, Map<KnownPlayer, ProgressionLevel> levelCache) {
            this.levelProgress = levelProgress;
            this.playerLevels = playerLevels;
            this.levelCache = levelCache;
            this.setDirty();
        }


        public static LevelProgressData load(CompoundTag tag) {
            final Map<KnownPlayer, Map<ProgressionLevel, LevelProgress>> levelProgress = new HashMap<>();
            final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();
            final Map<KnownPlayer, ProgressionLevel> levelCache = new HashMap<>();

            CompoundTag levelProgressTag = tag.getCompound("levelProgress");
            levelProgressTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
                KnownPlayer player = KnownPlayer.fromCompoundTag(levelProgressTag.getCompound(s));
                CompoundTag progressTag = levelProgressTag.getCompound(player.id() + "-progress");
                Map<ProgressionLevel, LevelProgress> map = new HashMap<>();
                progressTag.getAllKeys().forEach(s1 -> {
                    ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(s1));
                    LevelProgress progress = LevelProgress.loadFromCompoundTag(progressTag.getCompound(s1));
                    map.put(level, progress);
                });
                levelProgress.put(player, map);
            });
            CompoundTag playerLevelsTag = tag.getCompound("playerLevels");
            playerLevelsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
                KnownPlayer player = KnownPlayer.fromCompoundTag(playerLevelsTag.getCompound(s));
                ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(playerLevelsTag.getString(player.id() + "-level")));
                playerLevels.put(player, level);
            });
            CompoundTag levelCacheTag = tag.getCompound("levelCache");
            levelCacheTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
                KnownPlayer player = KnownPlayer.fromCompoundTag(levelCacheTag.getCompound(s));
                ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(levelCacheTag.getString(player.id() + "-level")));
                levelCache.put(player, level);
            });

            return new LevelProgressData(levelProgress, playerLevels, levelCache);
        }


        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
            CompoundTag levelProgressTag = new CompoundTag();
            this.levelProgress.forEach((player, levelProgress1) -> {
                levelProgressTag.put(player.id() + "-player", player.toCompoundTag());
                CompoundTag tag1 = new CompoundTag();
                levelProgress1.forEach((level, progress) -> tag1.put(level.getId().toString(), progress.saveToCompoundTag()));
                levelProgressTag.put(player.id() + "-progress", tag1);
            });
            tag.put("levelProgress", levelProgressTag);

            CompoundTag tag4 = new CompoundTag();
            this.playerLevels.forEach((knownPlayer, level) -> {
                tag4.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
                tag4.putString(knownPlayer.id() + "-level", level.getId().toString());
            });
            tag.put("playerLevels", tag4);

            CompoundTag tag5 = new CompoundTag();
            this.levelCache.forEach((player, level) -> {
                tag5.put(player.id() + "-player", player.toCompoundTag());
                tag5.putString(player.id() + "-level", level.getId().toString());
            });
            tag.put("levelCache", tag5);

            return tag;
        }


        public void updateLevelProgressData(Map<KnownPlayer, Map<ProgressionLevel, LevelProgress>> levelProgress) {
            this.levelProgress.putAll(levelProgress);
            this.setDirty();
        }


        public void updatePlayerLevels(Map<KnownPlayer, ProgressionLevel> playerLevels) {
            this.playerLevels.putAll(playerLevels);
            this.setDirty();
        }


        public void updateLevelCache(Map<KnownPlayer, ProgressionLevel> levelCache) {
            this.levelCache.putAll(levelCache);
            this.setDirty();
        }
    }
}
