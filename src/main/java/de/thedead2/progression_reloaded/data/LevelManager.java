package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.language.ChatMessageHandler;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

/**
 * Handles and tracks all known levels, there progress and there corresponding quests. There's only one instance
 * of this class active per world.
 * **/
public class LevelManager {
    private static LevelManager instance = null;
    private static final Marker MARKER = new MarkerManager.Log4jMarker("LevelManager");

    /**
     * Map of all known players and their corresponding level.
     * **/
    private final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();
    /**
     * Map of all known levels and their corresponding progress.
     * **/
    private final Map<ProgressionLevel, LevelProgress> levelProgress = new HashMap<>();

    private final List<ProgressionLevel> levelOrder = new ArrayList<>(ModRegistries.LEVELS.get().getValues());
    private final QuestManager questManager;

    private LevelManager(){
        instance = this;
        this.questManager = new QuestManager();
        init();
    }

    public void reset() {
        instance = null;
    }

    private void init(){
        loadLevelOrder();
        loadPlayerLevels();
        loadLevelProgress();
        this.updateStatus();
    }

    public static LevelManager create() {
        return new LevelManager();
    }

    public void updateStatus(){
        this.playerLevels.forEach((player, level) -> {
            LOGGER.debug(MARKER,"Updating level status for player: {}", player.name());
            SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
            LevelProgress progress = this.levelProgress.get(level);
            if(progress.isDone(player)){
                if(!progress.hasBeenRewarded(player)){
                    ChatMessageHandler.sendMessage("Congratulations " + player.name() + " for completing level " + level.getId().toString() + "!", true, singlePlayer.getPlayer(), ChatFormatting.BOLD, ChatFormatting.GOLD);
                    LOGGER.debug(MARKER,"Player {} completed level {}", player.name(), level.getId());
                    level.rewardPlayer(singlePlayer);
                    progress.setRewarded(player, true);
                    this.updateLevel(singlePlayer, level.getNextLevel());
                }
            }
            else questManager.updateStatus(player);
        });
    }

    public void updateLevel(SinglePlayer player, ResourceLocation nextLevel){
        if(nextLevel == null) return;
        player.updateProgressionLevel(ModRegistries.LEVELS.get().getValue(nextLevel));
        this.playerLevels.replace(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel());
        this.saveData();
        this.updateStatus();
    }

    private void loadPlayerLevels(){
        this.playerLevels.putAll(PlayerDataHandler.getProgressData().orElseThrow().getPlayerLevels());
        PlayerDataHandler.allPlayers().forEach(player -> this.playerLevels.putIfAbsent(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel()));
    }

    private void loadLevelProgress() {
        this.levelProgress.putAll(PlayerDataHandler.getProgressData().orElseThrow().getLevelProgress());
        ModRegistries.LEVELS.get().getValues().forEach(level -> this.levelProgress.computeIfAbsent(level, LevelProgress::new));
    }

    private void loadLevelOrder(){
        levelOrder.sort(Comparator.comparingInt(ProgressionLevel::getIndex));
    }

    public ProgressionLevel getLevelForQuest(ProgressionQuest quest){
        ResourceLocation id = quest.getId();
        return this.levelOrder.stream().filter(level -> level.getQuests().contains(id)).findAny().orElseThrow(() -> new IllegalArgumentException("Unknown level for quest: " + id));
    }

    /*public LevelProgress getOrStartProgress(ProgressionLevel level) {
        LevelProgress levelProgress = this.levelProgress.get(level);
        if (levelProgress == null) {
            levelProgress = new LevelProgress(level);
            this.startProgress(level, levelProgress);
        }
        return levelProgress;
    }

    private void startProgress(ProgressionLevel level, LevelProgress levelProgress) {
        this.levelProgress.put(level, levelProgress);
        this.saveData();
    }*/

    public static LevelManager getInstance() {
        return instance;
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
        questManager.saveData();
    }

    public QuestManager getQuestManager() {
        return this.questManager;
    }
}
