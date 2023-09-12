package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.events.ModEvents;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncQuestsPacket;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.misc.HashBiSetMultiMap;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


/**
 * Handles and tracks all quests of all players.
 **/

public class QuestManager {

    private static final HashBiSetMultiMap<ProgressionQuest, ProgressionQuest> questChildren = new HashBiSetMultiMap<>();

    private static final Marker MARKER = new MarkerManager.Log4jMarker("QuestManager");

    private static boolean childrenLoaded = false;

    private final QuestProgressData progressData;

    private final LevelManager levelManager;

    /**
     * All active quests of each player. A player can have multiple active quests at once.
     * A quest is active if it's parent has been completed and itself isn't completed yet.
     */
    private final Map<KnownPlayer, Set<ProgressionQuest>> activePlayerQuests;

    private final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> questProgress;


    QuestManager(DimensionDataStorage dataStorage, LevelManager levelManager) {
        this.progressData = dataStorage.computeIfAbsent(QuestProgressData::load, () -> new QuestProgressData(new HashMap<>(), new HashMap<>()), "questProgress");
        this.levelManager = levelManager;
        this.activePlayerQuests = new HashMap<>();
        this.questProgress = new HashMap<>();
        this.loadData();
    }


    private void loadData() {
        this.loadActivePlayerQuests();
        this.loadQuestProgress();
        this.loadChildren();
    }


    private void loadActivePlayerQuests() {
        this.activePlayerQuests.putAll(this.progressData.activeQuests);
        PlayerDataHandler.allPlayers().forEach(player -> this.activePlayerQuests.putIfAbsent(KnownPlayer.fromSinglePlayer(player), new HashSet<>()));
    }


    private void loadQuestProgress() {
        this.questProgress.putAll(this.progressData.playerProgress);
        PlayerDataHandler.allPlayers().forEach(player -> this.questProgress.putIfAbsent(KnownPlayer.fromSinglePlayer(player), new HashMap<>()));
    }


    private void loadChildren() {
        if(childrenLoaded) {
            return;
        }

        LOGGER.debug(MARKER, "Loading quest children cache...");
        ModRegistries.QUESTS.get().getValues().forEach(quest -> {
            ResourceLocation parentId = quest.getParentQuest();
            ProgressionQuest parentQuest = null;
            if(parentId != null) {
                parentQuest = this.findQuest(parentId);
            }
            else {
                ProgressionLevel level = LevelManager.getInstance().getLevelForQuest(quest);
                ResourceLocation previousLevelId = level.getPreviousLevel();
                if(previousLevelId != null) {
                    parentQuest = this.getLastMainQuestForLevel(ModRegistries.LEVELS.get().getValue(previousLevelId));
                }
            }

            if(parentQuest != null) {
                questChildren.compute(parentQuest, (quest1, progressionQuests) -> progressionQuests == null ? new HashSet<>() : progressionQuests).add(quest);
            }
        });

        childrenLoaded = true;
    }


    public ProgressionQuest findQuest(ResourceLocation questId) {
        return Optional.ofNullable(ModRegistries.QUESTS.get().getValue(questId)).orElseThrow(() -> new IllegalArgumentException("Unknown quest with id: " + questId));
    }


    private ProgressionQuest getLastMainQuestForLevel(ProgressionLevel level) {
        Collection<ProgressionQuest> levelQuests = level.getQuests()
                                                        .stream()
                                                        .map(this::findQuest)
                                                        .collect(Collectors.toSet());
        for(ProgressionQuest quest : levelQuests) {
            if(quest.isMainQuest() && this.findChildQuest(quest) == null) {
                return quest;
            }
        }
        return null;
    }


    private ProgressionQuest findChildQuest(ProgressionQuest quest) {
        ResourceLocation id = quest.getId();
        return ModRegistries.QUESTS.get()
                                   .getValues()
                                   .stream()
                                   .filter(quest1 -> quest1.getParentQuest() != null && quest1.getParentQuest().equals(id))
                                   .findAny()
                                   .orElse(null);
    }


    public void updateData() {
        this.saveData();
        this.resetData();
        this.loadData();
    }


    public void saveData() {
        this.progressData.updateActiveQuestsData(this.activePlayerQuests);
        this.progressData.updateQuestProgressData(this.questProgress);
    }


    private void resetData() {
        this.activePlayerQuests.clear();
        this.questProgress.clear();
    }


    public boolean isQuestActiveForLevel(ProgressionQuest quest, KnownPlayer player) {
        PlayerData activePlayer = PlayerDataHandler.getActivePlayer(player);
        if(activePlayer == null) {
            return false;
        }
        ProgressionLevel level = activePlayer.getProgressionLevel();
        return level.contains(quest) && isQuestActive(quest, player);
    }


    /**
     * Awards the given criterion to the given quest and if the quest is done, the quest to the given player
     **/
    public boolean award(ProgressionQuest quest, String criterionName, KnownPlayer player) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if(!ModEvents.onQuestAward(quest, criterionName, questProgress, PlayerDataHandler.getActivePlayer(player))) {
            boolean flag1 = questProgress.isDone();
            PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
            if(criterionName == null || questProgress.grantProgress(criterionName)) {
                if(criterionName == null) {
                    questProgress.complete();
                }
                this.unregisterListeners(quest, player);
                flag = true;
                if(!flag1 && questProgress.isDone()) {
                    quest.rewardPlayer(playerData); //TODO: Instead of directly rewarding the player, add reward to set --> reward on button press
                }
            }

            if(flag) {
                LevelManager.getInstance().updateStatus();
            }
        }

        return flag;
    }


    public void registerListeners(ProgressionQuest quest, KnownPlayer player) {
        //LOGGER.debug("Registering listeners for quest: {}" , quest.getId());
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if(!questProgress.isDone()) {
            for(Map.Entry<String, SimpleTrigger<?>> entry : quest.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
                if(criterionprogress != null && !criterionprogress.isDone()) {
                    SimpleTrigger<?> trigger = entry.getValue();
                    if(trigger != null) {
                        trigger.addListener(player, new SimpleTrigger.Listener(quest, entry.getKey()));
                    }
                }
            }
        }
    }


    private Set<ProgressionQuest> searchQuestsForActive(KnownPlayer player) {
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> isQuestActiveForLevel(progressionQuest, player)).collect(Collectors.toSet());
    }


    private Set<ProgressionQuest> searchQuestsForComplete(KnownPlayer player) {
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> !isQuestActiveForLevel(progressionQuest, player)).collect(Collectors.toSet());
    }


    @SuppressWarnings("unchecked")
    public <T> void fireTriggers(Class<? extends SimpleTrigger<T>> triggerClass, PlayerData player, T toTest, Object... data) {
        //        LOGGER.debug(MARKER,"Firing trigger: {}", triggerClass.getName());
        KnownPlayer knownPlayer = KnownPlayer.fromSinglePlayer(player);
        activePlayerQuests.get(knownPlayer)
                          .forEach(quest -> quest.getCriteria()
                                                 .values()
                                                 .stream()
                                                 .filter(simpleTrigger -> simpleTrigger.getClass().equals(triggerClass))
                                                 .forEach(trigger -> {
                                                     if(!ModEvents.onTriggerFiring((SimpleTrigger<T>) trigger, player, toTest, data) && ((SimpleTrigger<T>) trigger).trigger(player, toTest, data)) {
                                                         this.updateStatus(knownPlayer, true);
                                                     }
                                                 })
                          );
    }


    public boolean isQuestActive(ProgressionQuest quest, KnownPlayer player) {
        return quest.isParentDone(this, player) && !quest.isDone(this, player);
    }


    public void stopListening() {
        this.activePlayerQuests.forEach((player, progressionQuests) -> progressionQuests.forEach(quest -> this.unregisterListeners(quest, player)));
    }


    public void unregisterListeners(ProgressionQuest quest, KnownPlayer player) {
        //LOGGER.debug("Unregistering listeners for quest {} for player {}", quest.getId(), player.name());
        QuestProgress questProgress = this.getOrStartProgress(quest, player);

        for(Map.Entry<String, SimpleTrigger<?>> entry : quest.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
            if(criterionprogress != null && (criterionprogress.isDone() || questProgress.isDone())) {
                SimpleTrigger<?> trigger = entry.getValue();
                if(trigger != null) {
                    trigger.removeListener(player, new SimpleTrigger.Listener(quest, entry.getKey()));
                }
            }
        }
    }


    public Collection<QuestProgress> getMainQuestProgress(ProgressionLevel level, KnownPlayer player) {
        Collection<QuestProgress> questProgresses = new HashSet<>();
        this.getMainQuestsForLevel(level).forEach(quest -> {
            if(quest.isMainQuest()) {
                questProgresses.add(this.getOrStartProgress(quest, player));
            }
        });
        return questProgresses;
    }


    public Collection<ProgressionQuest> getMainQuestsForLevel(ProgressionLevel level) {
        return level.getQuests()
                    .stream()
                    .map(this::findQuest)
                    .collect(Collectors.toSet());
    }


    public QuestProgress getOrStartProgress(ProgressionQuest quest, KnownPlayer player) {
        QuestProgress questProgress = this.questProgress.get(player).get(quest);
        if(questProgress == null) {
            questProgress = new QuestProgress(quest);
            this.startProgress(quest, questProgress, player);
        }
        return questProgress;
    }


    private void startProgress(ProgressionQuest quest, QuestProgress questProgress, KnownPlayer player) {
        questProgress.updateProgress(quest);
        this.questProgress.computeIfAbsent(player, player1 -> new HashMap<>()).put(quest, questProgress);
        this.saveData();
    }


    public Collection<QuestProgress> getSideQuestProgress(ProgressionLevel level, KnownPlayer player) {
        Collection<QuestProgress> questProgresses = new HashSet<>();
        Collection<ProgressionQuest> levelQuests = level.getQuests()
                                                        .stream()
                                                        .map(this::findQuest)
                                                        .collect(Collectors.toSet());
        levelQuests.forEach(quest -> {
            if(!quest.isMainQuest()) {
                questProgresses.add(this.getOrStartProgress(quest, player));
            }
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


    /**
     * Updates the quests of a player depending on whether the quest has been completed or not.
     */
    public void updateStatus(KnownPlayer player, boolean shouldSyncToTeam) {
        LOGGER.debug(MARKER, "Updating quests for player: {}", player.name());

        this.activePlayerQuests.replace(player, searchQuestsForActive(player));
        searchQuestsForComplete(player).forEach(quest -> this.unregisterListeners(quest, player));
        this.activePlayerQuests.get(player).forEach(quest -> this.registerListeners(quest, player));

        if(shouldSyncToTeam) {
            this.syncQuestProgressToTeam(player);
        }
        this.syncQuestsToClient(player);
        ModEvents.onQuestStatusUpdate(player, this.activePlayerQuests.get(player));
    }


    public boolean revoke(ProgressionQuest quest, KnownPlayer player) {
        return revoke(quest, null, player);
    }


    public boolean revoke(ResourceLocation quest, KnownPlayer player) {
        return revoke(findQuest(quest), null, player);
    }


    /**
     * Revokes the given quest for the given player
     **/
    public boolean revoke(ProgressionQuest quest, String criterionName, KnownPlayer player) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest, player);
        if(!ModEvents.onQuestRevoke(quest, criterionName, questProgress, PlayerDataHandler.getActivePlayer(player))) {
            if(criterionName == null || questProgress.revokeProgress(criterionName)) {
                if(criterionName == null) {
                    questProgress.reset();
                }
                if(quest.isParentDone(this, player)) {
                    this.registerListeners(quest, player);
                }
                LevelManager.getInstance().updateStatus();
                flag = true;
            }
        }

        return flag;
    }


    private void syncQuestsToClient(KnownPlayer player) {
        LOGGER.debug(MARKER, "Attempting to sync quest status with client...");

        PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
        if(playerData == null) {
            LOGGER.debug(MARKER, "No client found to sync quest status with! Skipping...");
            return;
        }
        ModNetworkHandler.sendToPlayer(new ClientSyncQuestsPacket(this.activePlayerQuests.get(player), this.questProgress.get(player)), playerData.getServerPlayer());
    }


    private void syncQuestProgressToTeam(KnownPlayer player) {
        PlayerTeam team = PlayerDataHandler.getTeam(player);
        var questProgress = this.questProgress.get(player);
        if(team == null) {
            return;
        }
        team.forEachMember(player1 -> {
            if(!player1.equals(player)) {
                this.questProgress.compute(player1, (player2, map) -> map == null ? new HashMap<>() : map).putAll(
                        questProgress);
                this.updateStatus(player1, false);
            }
        });
    }


    public Collection<ProgressionQuest> getActiveQuests(KnownPlayer knownPlayer) {
        return this.activePlayerQuests.get(knownPlayer);
    }


    public boolean isParentDone(ProgressionQuest quest, KnownPlayer player) {
        ProgressionQuest parentQuest = questChildren.inverse().get(quest);
        if(parentQuest != null) {
            return this.getOrStartProgress(parentQuest, player).isDone();
        }
        return true;
    }


    public boolean hasChild(ProgressionQuest quest) {
        return questChildren.containsKey(quest);
    }


    public void removePlayerData(KnownPlayer player) {
        this.activePlayerQuests.remove(player);
        this.questProgress.remove(player);
    }


    private static final class QuestProgressData extends SavedData {

        private final Map<KnownPlayer, Set<ProgressionQuest>> activeQuests;

        private final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress;


        private QuestProgressData(Map<KnownPlayer, Set<ProgressionQuest>> activeQuests, Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress) {
            this.activeQuests = activeQuests;
            this.playerProgress = playerProgress;
        }


        private static QuestProgressData load(CompoundTag tag) {
            final Map<KnownPlayer, Set<ProgressionQuest>> activeQuests = new HashMap<>();
            final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> questProgress = new HashMap<>();

            CompoundTag activeQuestsTag = tag.getCompound("activeQuests");
            activeQuestsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
                KnownPlayer player = KnownPlayer.fromCompoundTag(activeQuestsTag.getCompound(s));
                CompoundTag tag1 = activeQuestsTag.getCompound(player.id() + "-activeQuests");
                Set<ProgressionQuest> quests = new HashSet<>();
                tag1.getAllKeys().forEach(s1 -> quests.add(ModRegistries.QUESTS.get().getValue(new ResourceLocation(s1))));
                activeQuests.put(player, quests);
            });

            CompoundTag questTag = tag.getCompound("questProgress");
            questTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
                KnownPlayer player = KnownPlayer.fromCompoundTag(questTag.getCompound(s));
                CompoundTag tag1 = questTag.getCompound(player.id() + "-progress");
                Map<ProgressionQuest, QuestProgress> progressMap = new HashMap<>();
                tag1.getAllKeys().forEach(s1 -> {
                    QuestProgress progress = QuestProgress.loadFromCompoundTag(tag1.getCompound(s1));
                    ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(new ResourceLocation(s1));
                    progressMap.put(quest, progress);
                });
                questProgress.put(player, progressMap);
            });

            return new QuestProgressData(activeQuests, questProgress);
        }


        @Override
        public @NotNull CompoundTag save(CompoundTag tag) {
            CompoundTag tag1 = new CompoundTag();
            this.activeQuests.forEach((knownPlayer, progressionQuests) -> {
                tag1.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
                CompoundTag tag2 = new CompoundTag();
                progressionQuests.forEach(quest -> tag2.putBoolean(quest.getId().toString(), quest.isMainQuest()));
                tag1.put(knownPlayer.id() + "-activeQuests", tag2);
            });
            tag.put("activeQuests", tag1);

            CompoundTag tag2 = new CompoundTag();
            this.playerProgress.forEach((knownPlayer, questProgressMap) -> {
                tag2.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
                CompoundTag tag3 = new CompoundTag();
                questProgressMap.forEach((quest, progress) -> tag3.put(
                        quest.getId().toString(),
                        progress.saveToCompoundTag()
                ));
                tag2.put(knownPlayer.id() + "-progress", tag3);
            });
            tag.put("questProgress", tag2);

            return tag;
        }


        public void updateQuestProgressData(Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress) {
            this.playerProgress.putAll(playerProgress);
            this.setDirty();
        }


        public void updateActiveQuestsData(Map<KnownPlayer, Set<ProgressionQuest>> activeQuests) {
            this.activeQuests.putAll(activeQuests);
            this.setDirty();
        }
    }
}
