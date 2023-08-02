package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import de.thedead2.progression_reloaded.util.language.ChatMessageHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

/**
 * Handles and tracks all known levels, there progress and there corresponding QuestManagers. There's only one instance
 * of this class active per world.
 * **/
public class LevelManager {
    private static LevelManager instance = null;

    /**
     * Map of all known players and their corresponding level.
     * **/
    private final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();
    /**
     * Map of all known levels and their corresponding progress.
     * **/
    private final Map<ProgressionLevel, LevelProgress> levelProgress = new HashMap<>();

    private final List<ProgressionLevel> levelOrder = new ArrayList<>(ModRegistries.LEVELS.get().getValues());

    private LevelManager(){
        instance = this;
        init();
    }

    public void reset() {
        convertQuestManagers();
        instance = null;
    }

    private void init(){
        loadLevelOrder();
        convertQuestManagers();
        loadPlayerLevels();
        loadLevelProgress();
        this.updateStatus();
    }

    private void convertQuestManagers() {
        this.levelOrder.forEach(level -> level.updateQuestManager(level.getQuestManager().convert()));
    }

    public static LevelManager create() {
        return new LevelManager();
    }

    public void updateStatus(){
        LOGGER.debug("Updating level status for player levels: {}", this.playerLevels);
        this.playerLevels.forEach((player, level) -> {
            SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
            LevelProgress progress = this.getOrStartProgress(level);
            if(progress.isDone(singlePlayer)){
                if(!progress.hasBeenRewarded(singlePlayer)){
                    ChatMessageHandler.sendMessage("Congratulations " + player.name() + " for completing this level!", true, singlePlayer.getPlayer(), ChatFormatting.BOLD, ChatFormatting.GOLD);
                    LOGGER.debug("Completed {} Progression Level", level.getId());
                    level.rewardPlayer(singlePlayer);
                    progress.setRewarded(singlePlayer, true);
                    this.updateLevel(singlePlayer, level.getNextLevel());
                }
            }
            else {
                level.getQuestManager().updateStatus(singlePlayer);
            }
        });
    }

    public void updateLevel(SinglePlayer player, ResourceLocation nextLevel){
        if(nextLevel == null) return;
        player.updateProgressionLevel(ModRegistries.LEVELS.get().getValue(nextLevel));
        this.playerLevels.replace(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel());
        PlayerDataHandler.getProgressData().orElseThrow().updatePlayerLevels(this.playerLevels);
        this.updateStatus();
    }

    private void loadPlayerLevels(){
        this.playerLevels.putAll(PlayerDataHandler.getProgressData().orElseThrow().getPlayerLevels());
        PlayerDataHandler.getPlayerData().orElseThrow().allPlayersData().forEach(player -> this.playerLevels.putIfAbsent(KnownPlayer.fromSinglePlayer(player), player.getProgressionLevel()));
    }

    private void loadLevelProgress() {
        this.levelProgress.putAll(PlayerDataHandler.getProgressData().orElseThrow().getLevelProgress());
        ModRegistries.LEVELS.get().getValues().forEach(level -> this.levelProgress.computeIfAbsent(level, t -> new LevelProgress(level)));
    }

    private void loadLevelOrder(){
        levelOrder.sort(Comparator.comparingInt(ProgressionLevel::getIndex));
    }

    public LevelProgress getOrStartProgress(ProgressionLevel level) {
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
    }

    public static LevelManager getInstance() {
        return instance;
    }

    public void updateData() {
        this.saveData();
        this.loadPlayerLevels();
        this.loadLevelProgress();
        this.levelOrder.forEach(level -> level.getQuestManager().reloadData());
        this.updateStatus();
    }

    public void saveData() {
        LOGGER.debug(new MarkerManager.Log4jMarker("LevelManager"), "Saving data!");
        ProgressData progressData = PlayerDataHandler.getProgressData().orElseThrow();
        progressData.updateLevelProgressData(this.levelProgress);
        progressData.updatePlayerLevels(this.playerLevels);
        this.levelOrder.forEach(level -> level.getQuestManager().saveData());
    }

    public void changeLevel(SinglePlayer player, ResourceLocation level) {
        player.getProgressionLevel().getQuestManager().stopListening(player); //TODO: quests of other levels don't get listen to --> why?
        this.updateLevel(player, level);
    }
}
