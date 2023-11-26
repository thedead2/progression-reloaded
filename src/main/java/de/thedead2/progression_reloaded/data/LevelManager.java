package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private final List<ProgressionLevel> levelOrder = new LinkedList<>(ModRegistries.LEVELS.get().getValues().stream().filter(progressionLevel -> !progressionLevel.equals(CREATIVE)).toList());


    private final QuestManager questManager;

    private final RestrictionManager restrictionManager;


    private LevelManager() {
        instance = this;
        this.questManager = new QuestManager(this);
        this.restrictionManager = new RestrictionManager();
        init();
    }


    public static LevelManager create() {
        return new LevelManager();
    }


    public RestrictionManager getRestrictionManager() {
        return restrictionManager;
    }


    public static LevelManager getInstance() {
        return instance;
    }


    @Nullable
    public Pair<Player, ProgressionLevel> getHighestPlayerLevel(Collection<? extends Player> players) {
        Triple<Integer, Player, ProgressionLevel> highestLevel = null;

        for(Player player : players) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            if(playerData == null) {
                continue;
            }
            ProgressionLevel level = playerData.getCurrentLevel();
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
        this.updateStatus();
    }


    private void loadLevelOrder() {
        Collections.sort(this.levelOrder);
    }


    public void updateLevel(PlayerData player, ProgressionLevel nextLevel) {
        if(nextLevel != null && !PREventFactory.onLevelUpdate(nextLevel, player, player.getCurrentLevel())) {
            PlayerDataManager.updateProgressionLevel(player, nextLevel);
        }
        this.updateStatus();
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
        PlayerDataManager.allPlayers().forEach(player -> {
            LOGGER.debug(MARKER, "Updating level status for player: {}", player.getName());

            ProgressionLevel level = player.getCurrentLevel();
            LevelProgress progress = player.getCurrentLevelProgress();

            if(progress.isDone() && !progress.hasBeenRewarded()) {
                level.rewardPlayer(player);
                progress.setRewarded(true);
                ProgressionLevel nextLevel = this.getNextLevel(level);
                LOGGER.debug(MARKER, "Player {} completed level {}", player.getName(), level.getId());
                this.updateLevel(player, nextLevel);
            }
            else {
                if(!progress.isDone() && progress.hasBeenRewarded()) {
                    progress.setRewarded(false);
                }
                this.syncLevelsToClient(player);
                PREventFactory.onLevelStatusUpdate(level, player, progress);
                questManager.updateStatus(player, true);
            }
        });
    }


    private void syncLevelsToClient(PlayerData player) {
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(player), player.getServerPlayer());
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


    public void updateData() {
        this.questManager.updateData();
    }


    public ProgressionLevel getLevelForQuest(ProgressionQuest quest) {
        ResourceLocation id = quest.getId();
        return this.levelOrder.stream()
                              .filter(level -> level.getQuests().contains(id))
                              .findAny()
                              .orElseThrow(() -> new IllegalArgumentException("Unknown level for quest: " + id));
    }


    public QuestManager getQuestManager() {
        return this.questManager;
    }


    /*private void syncLevelsToClient(KnownPlayer player, ProgressionLevel level) {
        LOGGER.debug(MARKER, "Attempting to sync level status with client...");
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(playerData == null) {
            LOGGER.debug(MARKER, "No client found to sync level status with! Skipping...");
            return;
        }
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(playerData), playerData.getServerPlayer());
        ModNetworkHandler.sendToPlayer(new ClientSyncLevelsPacket(level, this.levelProgress.get(player)), playerData.getServerPlayer());
    }*/


    public void revoke(PlayerData player, ProgressionLevel level) {
        if(!PREventFactory.onLevelRevoke(player, level)) {
            this.resetLevelProgress(player, level);
            this.updateLevel(player, level);
        }
    }


    private void resetLevelProgress(PlayerData player, ProgressionLevel level) {
        player.getLevelData().resetLevelProgress(level);

        ProgressionLevel nextLevel = this.getNextLevel(level);
        if(nextLevel != null) {
            resetLevelProgress(player, nextLevel);
        }
    }


    public void award(PlayerData player, ProgressionLevel level) {
        if(!PREventFactory.onLevelAward(player, level)) {
            player.getLevelData().completeLevelProgress(level);

            ProgressionLevel previousLevel = this.getPreviousLevel(level);
            if(previousLevel != null) {
                award(player, previousLevel);
            }
            this.updateStatus();
        }
    }


    private void onSurvivalChange(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.getLevelData().restoreCachedLevel();
    }


    private void onCreativeChange(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.getLevelData().updateAndCacheLevel(CREATIVE);
    }


    public void checkForCreativeMode(PlayerData playerData) {
        ServerPlayer player = playerData.getServerPlayer();
        if((player.gameMode.isCreative() || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() && !playerData.hasProgressionLevel(CREATIVE.getId())) {
            this.onCreativeChange(player);
        }
    }
}
