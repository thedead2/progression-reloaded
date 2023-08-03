package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.level.LevelManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

/**
 * Handles and tracks all quests of the active level of a player. This includes all incomplete quests of other levels for each player.
 * Each level has its own QuestManager.
 * **/

public class ActiveQuestManager implements QuestManager {
    /** All quests of the level **/
    private final ImmutableMap<ResourceLocation, ProgressionQuest> quests;
    /** Additional quests of other levels that haven't been completed yet **/
    private final Map<ResourceLocation, ProgressionQuest> additionalQuests = new HashMap<>();
    /** All active quests of each player that has this level. A player can have multiple active quests at once.
     * A quest is active if it's parent has been completed and itself isn't completed yet.
     * **/
    private final Multimap<SinglePlayer, ProgressionQuest> activeQuests = HashMultimap.create();

    /**
     * All main quest progress info per player.
     * **/
    private final Multimap<SinglePlayer, QuestProgress> mainQuestsProgress = HashMultimap.create();
    /**
     * All side quest progress info per player.
     * **/
    private final Multimap<SinglePlayer, QuestProgress> sideQuestsProgress = HashMultimap.create();

    /** The resource location of the corresponding level for this quest manager **/
    private final ResourceLocation levelId;
    private final Marker marker;

    public ActiveQuestManager(Set<ResourceLocation> quests, ResourceLocation levelId){
        this.levelId = levelId;
        marker = new MarkerManager.Log4jMarker("QuestManager/" + this.levelId.getPath());
        Map<ResourceLocation, ProgressionQuest> questMap = new HashMap<>();
        quests.forEach(resourceLocation -> questMap.put(resourceLocation, ModRegistries.QUESTS.get().getValue(resourceLocation)));

        this.quests = ImmutableMap.copyOf(questMap);
        this.loadData();
    }

    private void loadData(){
        //loadAdditionalQuests();
        loadActiveQuests();
        loadQuestProgress();
    }

    /*private void loadAdditionalQuests() {
        ProgressionLevel thisLevel = ModRegistries.LEVELS.get().getValue(this.levelId);
        if(thisLevel.getPreviousLevel() == null) return;

        ProgressionLevel previousLevel = ModRegistries.LEVELS.get().getValue(thisLevel.getPreviousLevel());
        QuestManager previousQuestManager = previousLevel.getQuestManager();
        PlayerDataHandler.allPlayers().stream().filter(player -> player.hasProgressionLevel(thisLevel)).forEach(player -> this.loadAdditionalActiveQuests(previousQuestManager, player));
    }*/

    private void loadActiveQuests() {
        var activeQuests = PlayerDataHandler.getProgressData().orElseThrow().getActiveQuests(this.getAllQuests());
        activeQuests.keySet().forEach(knownPlayer -> {
            SinglePlayer player = PlayerDataHandler.getActivePlayer(knownPlayer);
            if(player != null){
                this.activeQuests.putAll(player, activeQuests.get(knownPlayer));
            }
        });

        PlayerDataHandler.getPlayerData().orElseThrow().allPlayersData().forEach(player -> {
            if(!this.activeQuests.containsKey(player)) {
                this.activeQuests.putAll(player, searchQuestsForActive(player));
            }
        });

        LOGGER.debug(marker,"Loaded active quests: {}", this.activeQuests);
    }

    private void loadQuestProgress() {
        var questProgress = PlayerDataHandler.getProgressData().orElseThrow().getQuestProgress(this.getAllQuests());
        questProgress.keySet().forEach(knownPlayer -> {
            SinglePlayer player = PlayerDataHandler.getActivePlayer(knownPlayer);
            if(player != null){
                questProgress.get(knownPlayer).forEach(questProgress1 -> {
                    ProgressionQuest quest = questProgress1.getQuest();
                    if(quest.isMainQuest()) this.mainQuestsProgress.put(player, questProgress1);
                    else this.sideQuestsProgress.put(player, questProgress1);
                });
            }
        });
    }

    @Override
    public void saveData(){
        ProgressData progressData = PlayerDataHandler.getProgressData().orElseThrow();
        progressData.updateQuestProgressData(this.mainQuestsProgress, this.sideQuestsProgress);
        progressData.updateActiveQuestsData(this.activeQuests);
    }

    @Override
    public void reloadData(){
        this.saveData();
        this.resetData();
        this.loadData();
    }

    private void resetData() {
        this.activeQuests.clear();
        this.additionalQuests.clear();
        this.mainQuestsProgress.clear();
        this.sideQuestsProgress.clear();
    }


    /**
     * Updates the active quests of a player depending on whether the quest has been completed or not.
     * **/
    @Override
    public void updateStatus(SinglePlayer player) {
        LOGGER.debug(marker, "Updating quests for player: {}", player.getPlayerName());
        LOGGER.debug(marker,"Currently all active player quests: {}", activeQuests);
        Collection<ProgressionQuest> activePlayerQuests = activeQuests.get(player);
        /*LOGGER.debug(marker,"Currently active player quests: {}", activePlayerQuests);
        activeQuests.removeAll(activePlayerQuests);
        LOGGER.debug(marker,"Currently all active player quests after removal: {}", activeQuests);
        */activePlayerQuests.forEach(progressionQuest -> {
            if(this.getOrStartProgress(progressionQuest, player).isDone()){
                LOGGER.debug(marker,"Quest progress is done for quest {} for player {}", progressionQuest.getId(), player.getPlayerName());
                this.unregisterListeners(progressionQuest, player);
                progressionQuest.setActive(false, player);
            }
            else {
                LOGGER.warn(marker,"Quest progress is not done for quest {} for player {}", progressionQuest.getId(), player.getPlayerName());
                progressionQuest.setActive(true, player);
            }
        });
        activePlayerQuests.removeIf(progressionQuest -> !progressionQuest.isActive(player));
        LOGGER.debug(marker,"Currently active player quests after removal: {}", activePlayerQuests);
        searchQuestsForActive(player).forEach(quest -> quest.setActive(true, player));
        activePlayerQuests.addAll(this.getAllQuests().stream().filter(quest -> quest.isActive(player)).toList());
        LOGGER.debug(marker,"Currently active player quests after adding: {}", activePlayerQuests);
        activePlayerQuests.forEach(quest -> this.registerListeners(quest, player));
        LOGGER.debug(marker,"all Currently active player quests: {}", activeQuests);
    }

    private Collection<ProgressionQuest> searchQuestsForActive(SinglePlayer player){
        return this.getAllQuests().stream().filter(progressionQuest -> {
            LOGGER.debug(marker,"Quest: " + progressionQuest.getId());
            LOGGER.debug(marker,"is quest done: " + progressionQuest.isDone(this, player));
            return ((!progressionQuest.hasParent() || progressionQuest.isParentDone(this, player)) && !progressionQuest.isDone(this, player));
        }).toList();
    }

    private Collection<ProgressionQuest> getAllQuests(){
        Collection<ProgressionQuest> quests = new HashSet<>();
        quests.addAll(this.quests.values());
        quests.addAll(this.additionalQuests.values());
        return quests;
    }


    public boolean award(ResourceLocation quest, String criterionName, SinglePlayer player) {
        return award(findQuest(quest), criterionName, player);
    }
    public boolean award(ResourceLocation quest, SinglePlayer player) {
        return award(findQuest(quest), null, player);
    }

    public boolean award(ProgressionQuest quest, SinglePlayer player) {
        return award(quest, null, player);
    }


    public void stopListening(SinglePlayer player) {
        this.activeQuests.get(player).forEach(quest -> this.unregisterListeners(quest, player));
    }

    /*@Override
    public void loadAdditionalActiveQuests(QuestManager questManager, SinglePlayer player) {
        Map<ProgressionQuest, QuestProgress> remainingQuests = questManager.getRemainingQuests(player);
        remainingQuests.forEach((quest, progress) -> {
            this.additionalQuests.put(quest.getId(), quest);
            if(quest.isMainQuest()) this.mainQuestsProgress.put(player, progress);
            else this.sideQuestsProgress.put(player, progress);
        });
    }*/

    /** Returns the remaining active quests with their corresponding progress data for the given player **/
    @Override
    public Map<ProgressionQuest, QuestProgress> getRemainingQuests(SinglePlayer player) {
        Map<ProgressionQuest, QuestProgress> questProgressMap = new HashMap<>();
        this.activeQuests.get(player).forEach(quest -> {
            QuestProgress progress = this.getOrStartProgress(quest, player);
            questProgressMap.put(quest, progress);
        });
        return questProgressMap;
    }

    @Override
    public ResourceLocation getLevel() {
        return this.levelId;
    }


    /** Awards the given criterion to the given quest and if the quest is done, the quest to the given player **/
    public boolean award(ProgressionQuest quest, String criterionName, SinglePlayer player) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        boolean flag1 = questProgress.isDone();
        if (criterionName != null && questProgress.grantProgress(criterionName)) {
            this.unregisterListeners(quest, player);
            flag = true;
            if (!flag1 && questProgress.isDone()) {
                quest.rewardPlayer(player);
                quest.setActive(false, player);
            }
        }
        else if (criterionName == null){
            questProgress.complete();
            this.unregisterListeners(quest, player);
            flag = true;
            if (!flag1 && questProgress.isDone()) {
                quest.rewardPlayer(player);
                quest.setActive(false, player);
            }
        }

        if(flag) LevelManager.getInstance().updateStatus();

        return flag;
    }

    /** Revokes the given quest for the given player **/
    public boolean revoke(ProgressionQuest quest, SinglePlayer player, String criterionName) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if (questProgress.revokeProgress(criterionName) && quest.isParentDone(this, player)) {
            quest.setActive(true, player);
            this.registerListeners(quest, player);
            this.updateStatus(player);
            flag = true;
        }

        return flag;
    }

    public QuestProgress getOrStartProgress(ProgressionQuest quest, SinglePlayer player) {
        QuestProgress questProgress = this.getProgress(quest, player);
        if (questProgress == null) {
            questProgress = new QuestProgress(quest);
            this.startProgress(player, questProgress);
        }

        return questProgress;
    }

    public QuestProgress getProgress(ProgressionQuest quest, SinglePlayer player){
        return this.getCorrectQuestMap(quest).get(player).stream().filter(questProgress -> questProgress.getQuest().equals(quest)).findFirst().orElse(null);
    }

    private Multimap<SinglePlayer, QuestProgress> getCorrectQuestMap(ProgressionQuest quest) {
        if(quest.isMainQuest()) return this.mainQuestsProgress;
        else return this.sideQuestsProgress;
    }

    private void startProgress(SinglePlayer player, QuestProgress questProgress) {
        questProgress.updateProgress();
        this.getCorrectQuestMap(questProgress.getQuest()).put(player, questProgress);
        this.saveData();
    }


    private void registerListeners(ProgressionQuest quest, SinglePlayer player) {
        LOGGER.debug(marker,"Registering listeners for quest: {}" , quest.getId());
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

    private void unregisterListeners(ProgressionQuest quest, SinglePlayer player) {
        LOGGER.debug(marker,"Unregistering listeners for quest {} for player {}", quest.getId(), player.getPlayerName());
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

    public ProgressionQuest findQuest(ResourceLocation questId){
        return Optional.ofNullable(this.quests.get(questId)).orElseGet(() -> Optional.ofNullable(additionalQuests.get(questId)).orElseThrow(() -> new IllegalArgumentException("Unknown quest with id " + questId.toString()/* + " for level " + levelId.toString()*/)));
    }

    public <T extends SimpleTrigger> void fireTriggers(Class<T> triggerClass, SinglePlayer player, Object... data) {
        LOGGER.debug(marker,"Firing trigger: {}", triggerClass.getName());
        new HashSet<>(activeQuests.get(player)).forEach(quest -> quest.getCriteria().forEach((s, trigger) -> {
            if (trigger.getClass().equals(triggerClass)){
                trigger.trigger(player, data);
            }
        }));
        this.updateStatus(player);
    }

    public ImmutableSet<QuestProgress> getMainQuestProgressFor(SinglePlayer player) {
        return ImmutableSet.copyOf(this.mainQuestsProgress.get(player));
    }

    public ImmutableMap<ResourceLocation, ProgressionQuest> getQuests(){
        return this.quests;
    }

    @Override
    public PreQuestManager convert() {
        return new PreQuestManager(this.quests.keySet(), levelId);
    }

    public boolean isEmpty() {
        return this.quests.isEmpty();
    }

    public int size() {
        return this.quests.size();
    }

    public JsonElement toJson() {
        JsonArray jsonArray = new JsonArray();
        this.quests.keySet().forEach(resourceLocation -> jsonArray.add(resourceLocation.toString()));
        return jsonArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveQuestManager that = (ActiveQuestManager) o;
        return com.google.common.base.Objects.equal(quests, that.quests) && com.google.common.base.Objects.equal(activeQuests, that.activeQuests) && com.google.common.base.Objects.equal(mainQuestsProgress, that.mainQuestsProgress) && com.google.common.base.Objects.equal(sideQuestsProgress, that.sideQuestsProgress);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(quests, activeQuests, mainQuestsProgress, sideQuestsProgress);
    }
}
