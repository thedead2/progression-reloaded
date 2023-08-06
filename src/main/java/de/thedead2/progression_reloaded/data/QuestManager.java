package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.*;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;
import java.util.stream.Collectors;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

/**
 * Handles and tracks all quests of all players.
 * **/

public class QuestManager {
    /** All active quests of each player. A player can have multiple active quests at once.
     * A quest is active if it's parent has been completed and itself isn't completed yet.
     * */
    private final Map<KnownPlayer, Set<ProgressionQuest>> activePlayerQuests;
    private final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> questProgress;
    //TODO: maybe hold Map of Quests parents and children to save time when accessing?
    private static final Marker MARKER = new MarkerManager.Log4jMarker("QuestManager");

    QuestManager(){
        this.activePlayerQuests = new HashMap<>();
        this.questProgress = new HashMap<>();
        this.loadData();
    }

    private void loadData(){
        this.loadActivePlayerQuests();
        this.loadQuestProgress();
    }

    private void loadQuestProgress() {
        this.questProgress.putAll(PlayerDataHandler.getProgressData().orElseThrow().getQuestProgressData());
        PlayerDataHandler.allPlayers().forEach(player -> this.questProgress.putIfAbsent(KnownPlayer.fromSinglePlayer(player), new HashMap<>()));
    }

    private void loadActivePlayerQuests(){
        this.activePlayerQuests.putAll(PlayerDataHandler.getProgressData().orElseThrow().getActivePlayerQuests());
        PlayerDataHandler.allPlayers().forEach(player -> this.activePlayerQuests.putIfAbsent(KnownPlayer.fromSinglePlayer(player), new HashSet<>()));
    }
    public void saveData(){
        ProgressData progressData = PlayerDataHandler.getProgressData().orElseThrow();
        progressData.updateActiveQuestsData(this.activePlayerQuests);
        progressData.updateQuestProgressData(this.questProgress);
    }
    public void updateData(){
        this.saveData();
        this.resetData();
        this.loadData();
    }

    private void resetData() {
        this.activePlayerQuests.clear();
        this.questProgress.clear();
    }


    /**
     * Updates the quests of a player depending on whether the quest has been completed or not.
     * */
    public void updateStatus(KnownPlayer player) {
        LOGGER.debug(MARKER, "Updating quests for player: {}", player.name());
        this.activePlayerQuests.replace(player, searchQuestsForActive(player));
        searchQuestsForComplete(player).forEach(quest -> this.unregisterListeners(quest, player));
        this.activePlayerQuests.get(player).forEach(quest -> this.registerListeners(quest, player));
    }

    public void registerListeners(ProgressionQuest quest, KnownPlayer player) {
        LOGGER.debug("Registering listeners for quest: {}" , quest.getId());
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if (!questProgress.isDone()) {
            for(Map.Entry<String, SimpleTrigger> entry : quest.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
                if (criterionprogress != null && !criterionprogress.isDone()) {
                    SimpleTrigger trigger = entry.getValue();
                    if (trigger != null) {
                        trigger.addListener(player, new SimpleTrigger.Listener(quest, entry.getKey()));
                    }
                }
            }
        }
    }

    public void unregisterListeners(ProgressionQuest quest, KnownPlayer player) {
        LOGGER.debug("Unregistering listeners for quest {} for player {}", quest.getId(), player.name());
        QuestProgress questProgress = this.getOrStartProgress(quest, player);

        for(Map.Entry<String, SimpleTrigger> entry : quest.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
            if (criterionprogress != null && (criterionprogress.isDone() || questProgress.isDone())) {
                SimpleTrigger trigger = entry.getValue();
                if (trigger != null) {
                    trigger.removeListener(player, new SimpleTrigger.Listener(quest, entry.getKey()));
                }
            }
        }
    }

    private Set<ProgressionQuest> searchQuestsForActive(KnownPlayer player){
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> isQuestActiveForLevel(progressionQuest, player)).collect(Collectors.toSet());
    }
    private Set<ProgressionQuest> searchQuestsForComplete(KnownPlayer player){
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> !isQuestActiveForLevel(progressionQuest, player)).collect(Collectors.toSet());
    }

    public boolean isQuestActiveForLevel(ProgressionQuest quest, KnownPlayer player){
        SinglePlayer activePlayer = PlayerDataHandler.getActivePlayer(player);
        if(activePlayer == null) return false;
        ProgressionLevel level = activePlayer.getProgressionLevel();
        return level.contains(quest) && isQuestActive(quest, player);
    }

    public boolean isQuestActive(ProgressionQuest quest, KnownPlayer player){
        LOGGER.debug("Quest: " + quest.getId());
        LOGGER.debug("is quest done: " + quest.isDone(this, player));
        return quest.isParentDone(this, player) && !quest.isDone(this, player);
    }

    public void stopListening() {
        this.activePlayerQuests.forEach((player, progressionQuests) -> progressionQuests.forEach(quest -> this.unregisterListeners(quest, player)));
    }

    public Collection<QuestProgress> getMainQuestProgress(ProgressionLevel level, KnownPlayer player){
        Collection<QuestProgress> questProgresses = new HashSet<>();
        Collection<ProgressionQuest> levelQuests = level.getQuests().stream().map(this::findQuest).collect(Collectors.toSet());
        levelQuests.forEach(quest -> {
            if(quest.isMainQuest()) questProgresses.add(this.getOrStartProgress(quest, player));
        });
        return questProgresses;
    }
    public Collection<QuestProgress> getSideQuestProgress(ProgressionLevel level, KnownPlayer player){
        Collection<QuestProgress> questProgresses = new HashSet<>();
        Collection<ProgressionQuest> levelQuests = level.getQuests().stream().map(this::findQuest).collect(Collectors.toSet());
        levelQuests.forEach(quest -> {
            if(!quest.isMainQuest()) questProgresses.add(this.getOrStartProgress(quest, player));
        });
        return questProgresses;
    }

    public boolean award(ResourceLocation quest, String criterionName, KnownPlayer player) {
        return award(findQuest(quest), criterionName, player);
    }
    public boolean award(ResourceLocation quest, KnownPlayer player) {
        return award(findQuest(quest), null, player);
    }

    public boolean award(ProgressionQuest quest, KnownPlayer player) {
        return award(quest, null, player);
    }

    public ProgressionQuest findQuest(ResourceLocation questId){
        return Optional.ofNullable(ModRegistries.QUESTS.get().getValue(questId)).orElseThrow(() -> new IllegalArgumentException("Unknown quest with id: " + questId));
    }


    /** Awards the given criterion to the given quest and if the quest is done, the quest to the given player **/
    public boolean award(ProgressionQuest quest, String criterionName, KnownPlayer player) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        boolean flag1 = questProgress.isDone();
        SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
        if (criterionName == null || questProgress.grantProgress(criterionName)) {
            if(criterionName == null) questProgress.complete();
            this.unregisterListeners(quest, player);
            flag = true;
            if (!flag1 && questProgress.isDone()) {
                quest.rewardPlayer(singlePlayer);
            }
        }

        if(flag) LevelManager.getInstance().updateStatus();

        return flag;
    }

    /** Revokes the given quest for the given player **/
    public boolean revoke(ProgressionQuest quest, String criterionName, KnownPlayer player) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if (questProgress.revokeProgress(criterionName) && quest.isParentDone(this, player)) {
            this.registerListeners(quest, player);
            LevelManager.getInstance().updateStatus();
            flag = true;
        }

        return flag;
    }

    public QuestProgress getOrStartProgress(ProgressionQuest quest, KnownPlayer player) {
        QuestProgress questProgress = this.questProgress.get(player).get(quest);
        if (questProgress == null) {
            questProgress = new QuestProgress(quest);
            this.startProgress(quest, questProgress, player);
        }

        return questProgress;
    }

    private void startProgress(ProgressionQuest quest, QuestProgress questProgress, KnownPlayer player) {
        questProgress.updateProgress();
        this.questProgress.computeIfAbsent(player, player1 -> new HashMap<>()).put(quest, questProgress);
        this.saveData();
    }


    public <T extends SimpleTrigger> void fireTriggers(Class<T> triggerClass, SinglePlayer player, Object... data) {
        LOGGER.debug(MARKER,"Firing trigger: {}", triggerClass.getName());
        KnownPlayer knownPlayer = KnownPlayer.fromSinglePlayer(player);
        activePlayerQuests.get(knownPlayer).forEach(quest -> quest.getCriteria().forEach((s, trigger) -> {
            if (trigger.getClass().equals(triggerClass)){
                trigger.trigger(player, data);
            }
        }));
        this.updateStatus(knownPlayer);
    }

    public ProgressionQuest getLastMainQuestForLevel(ProgressionLevel level) {
        Collection<ProgressionQuest> levelQuests = level.getQuests().stream().map(this::findQuest).collect(Collectors.toSet());
        for (ProgressionQuest quest : levelQuests) {
            if(quest.isMainQuest() && !quest.hasChild()) return quest;
        }
        return null;
    }

    public ProgressionQuest findChildQuest(ProgressionQuest quest) {
        ResourceLocation id = quest.getId();
        return ModRegistries.QUESTS.get().getValues().stream().filter(quest1 -> quest1.getParentQuest() != null && quest1.getParentQuest().equals(id)).findAny().orElse(null);
    }
}
