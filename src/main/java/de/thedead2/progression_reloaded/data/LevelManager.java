package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.events.ModEvents;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.PlayerTeamSynchronizer;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.language.ChatMessageHandler;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<ProgressionLevel, LevelProgress> levelProgress = new HashMap<>();

    private final List<ProgressionLevel> levelOrder = new ArrayList<>(ModRegistries.LEVELS.get().getValues());

    private final Map<KnownPlayer, ProgressionLevel> levelCache = new HashMap<>();

    private final QuestManager questManager;


    private LevelManager() {
        instance = this;
        this.questManager = new QuestManager();
        init();
    }


    public static LevelManager create() {
        return new LevelManager();
    }


    public static LevelManager getInstance() {
        return instance;
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
        loadPlayerLevels();
        loadLevelProgress();
        loadLevelCache();
        this.updateStatus();
    }


    public void updateStatus() {
        this.playerLevels.forEach((player, level) -> {
            LOGGER.debug(MARKER, "Updating level status for player: {}", player.name());
            SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
            LevelProgress progress = this.levelProgress.get(level);
            ModEvents.onLevelStatusUpdate(level, singlePlayer, progress);
            if(progress.isDone(player)) {
                if(!progress.hasBeenRewarded(player)) {
                    ChatMessageHandler.sendMessage("Congratulations " + player.name() + " for completing level " + level.getId()
                                                                                                                        .toString() + "!", true, singlePlayer.getServerPlayer(), ChatFormatting.BOLD, ChatFormatting.GOLD);
                    LOGGER.debug(MARKER, "Player {} completed level {}", player.name(), level.getId());
                    level.rewardPlayer(singlePlayer);
                    progress.setRewarded(player, true);
                    this.updateLevel(singlePlayer, level.getNextLevel());
                }
            }
            else {
                questManager.updateStatus(player, true);
            }
        });
    }


    public void updateLevel(SinglePlayer player, ResourceLocation nextLevel) {
        if(nextLevel != null) {
            this.updateLevel(player, ModRegistries.LEVELS.get().getValue(nextLevel));
        }
        else {
            this.updateStatus();
        }
    }


    public void updateLevel(SinglePlayer player, ProgressionLevel nextLevel) {
        if(nextLevel != null && !ModEvents.onLevelUpdate(nextLevel, player, this.playerLevels.get(KnownPlayer.fromSinglePlayer(player)))) {
            PlayerTeamSynchronizer.updateProgressionLevel(player, nextLevel);
            this.playerLevels.put(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel());
            this.syncLevels(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel());
            this.saveData();
        }
        this.updateStatus();
    }


    private void loadPlayerLevels() {
        this.playerLevels.putAll(PlayerDataHandler.getProgressData().orElseThrow().getPlayerLevels());
        PlayerDataHandler.allPlayers().forEach(player -> this.playerLevels.putIfAbsent(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel()));
    }


    private void loadLevelProgress() {
        this.levelProgress.putAll(PlayerDataHandler.getProgressData().orElseThrow().getLevelProgress());
        ModRegistries.LEVELS.get().getValues().forEach(level -> this.levelProgress.computeIfAbsent(level, LevelProgress::new));
    }


    private void loadLevelCache() {
        this.levelCache.putAll(PlayerDataHandler.getProgressData().orElseThrow().getLevelCache());
    }


    public ProgressionLevel getLevelForQuest(ProgressionQuest quest) {
        ResourceLocation id = quest.getId();
        return this.levelOrder.stream()
                              .filter(level -> level.getQuests().contains(id))
                              .findAny()
                              .orElseThrow(() -> new IllegalArgumentException("Unknown level for quest: " + id));
    }


    public void updateData() {
        this.saveData();
        this.loadPlayerLevels();
        this.loadLevelProgress();
        questManager.updateData();
        this.updateStatus();
    }


    public void saveData() {
        LOGGER.debug(MARKER, "Saving data!");
        ProgressData progressData = PlayerDataHandler.getProgressData().orElseThrow();
        progressData.updateLevelProgressData(this.levelProgress);
        progressData.updatePlayerLevels(this.playerLevels);
        progressData.updateLevelCache(this.levelCache);
        questManager.saveData();
    }


    public QuestManager getQuestManager() {
        return this.questManager;
    }


    private void syncLevels(KnownPlayer player, ProgressionLevel level) {
        PlayerTeam team = PlayerDataHandler.getTeam(player);
        if(team == null) {
            return;
        }
        team.forEachMember(player1 -> this.playerLevels.put(player1, level));
    }


    public void revoke(SinglePlayer player, ResourceLocation level) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(level);
        if(!ModEvents.onLevelRevoke(player, level1)) {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            this.levelProgress.get(level1).setRewarded(player1, false);
            level1.getQuests().forEach(id -> this.questManager.revoke(id, player1));
            this.updateLevel(player, level1);
        }
    }


    public void award(SinglePlayer player, ResourceLocation level) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(level);
        if(!ModEvents.onLevelAward(player, level1)) {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            level1.getQuests().forEach(id -> this.questManager.award(id, player1));
            this.updateLevel(player, level1);
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


    public void checkForCreativeMode(SinglePlayer singlePlayer) {
        ServerPlayer player = singlePlayer.getServerPlayer();
        if((player.gameMode.isCreative() || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() && !singlePlayer.hasProgressionLevel(TestLevels.CREATIVE.getId())) {
            this.onCreativeChange(player);
        }
    }
}
