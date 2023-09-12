package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.events.ModEvents;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncLevelsPacket;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerPacket;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.PlayerTeamSynchronizer;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.language.ChatMessageHandler;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.ChatFormatting;
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

    private final List<ProgressionLevel> levelOrder = new ArrayList<>(ModRegistries.LEVELS.get().getValues());

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
        return this.playerLevels.getOrDefault(player, TestLevels.CREATIVE);
    }


    public Pair<Player, ProgressionLevel> getHighestPlayerLevel(Collection<? extends Player> players) {
        Player player1;
        ProgressionLevel level1;
        List<Triple<Integer, Player, ProgressionLevel>> levels = new ArrayList<>();

        for(Player player : players) {
            PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
            if(playerData == null) {
                continue;
            }
            ProgressionLevel level = playerData.getProgressionLevel();
            levels.add(Triple.of(this.levelOrder.indexOf(level), player, level));
        }

        levels.sort(Comparator.comparingInt(Triple::getLeft));

        var triple = levels.get(levels.size() - 1);
        player1 = triple.getMiddle();
        level1 = triple.getRight();

        return Pair.of(player1, level1);
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

        if(nextLevel != null && !ModEvents.onLevelUpdate(nextLevel, player, this.playerLevels.get(knownPlayer))) {
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
                ChatMessageHandler.sendMessage("Congratulations " + player.name() + " for completing level " + level.getId().toString() + "!", true, playerData.getServerPlayer(), ChatFormatting.BOLD, ChatFormatting.GOLD);
                LOGGER.debug(MARKER, "Player {} completed level {}", player.name(), level.getId());
                level.rewardPlayer(playerData);
                progress.setRewarded(true);
                this.updateLevel(playerData, this.getNextLevel(level));
            }
            else {
                this.syncLevelsToClient(player, level);
                ModEvents.onLevelStatusUpdate(level, playerData, progress);
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


    @Nullable
    private ProgressionLevel getNextLevel(ProgressionLevel level) {
        int i = this.levelOrder.indexOf(level) + 1;
        if(this.levelOrder.size() < i) {
            return null;
        }
        else {
            return this.levelOrder.get(i);
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


    public void revoke(PlayerData player, ResourceLocation level) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(level);
        if(!ModEvents.onLevelRevoke(player, level1)) {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            LevelProgress progress = this.levelProgress.get(player1).get(level1);
            progress.reset();
            ProgressionLevel nextLevel = this.getNextLevel(level1);
            if(nextLevel != null) {
                revoke(player, nextLevel.getId());
            }
            this.updateStatus();
        }
    }


    public void award(PlayerData player, ResourceLocation level) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(level);
        if(!ModEvents.onLevelAward(player, level1)) {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            LevelProgress levelProgress = this.levelProgress.get(player1).get(level1);
            levelProgress.complete();
            ResourceLocation previousLevel = level1.getPreviousLevel();
            if(previousLevel != null) {
                award(player, previousLevel);
            }
            this.updateStatus();
        }
    }


    private void onSurvivalChange(Player entity) {
        KnownPlayer player = KnownPlayer.fromPlayer(entity);
        ProgressionLevel level = levelCache.remove(player);
        this.updateLevel(PlayerDataHandler.getActivePlayer(entity), level != null ? level.getId() : ResourceLocationHelper.getOrDefault(ConfigManager.DEFAULT_STARTING_LEVEL.get(), TestLevels.CREATIVE.getId()));
    }


    private void onCreativeChange(Player entity) {
        KnownPlayer player = KnownPlayer.fromPlayer(entity);
        ProgressionLevel currentLevel = playerLevels.get(player);
        levelCache.put(player, currentLevel);
        this.updateLevel(PlayerDataHandler.getActivePlayer(entity), TestLevels.CREATIVE);
    }


    public void checkForCreativeMode(PlayerData playerData) {
        ServerPlayer player = playerData.getServerPlayer();
        if((player.gameMode.isCreative() || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() && !playerData.hasProgressionLevel(TestLevels.CREATIVE.getId())) {
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
