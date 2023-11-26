package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientDisplayProgressToast;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.misc.HashBiSetMultiMap;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;
import java.util.stream.Collectors;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


/**
 * Handles and tracks all quests of all players.
 **/

public class QuestManager {

    private static final HashBiSetMultiMap<ProgressionQuest, ProgressionQuest> questChildren = new HashBiSetMultiMap<>();

    private static final Marker MARKER = new MarkerManager.Log4jMarker("QuestManager");

    private boolean childrenLoaded = false;
    private final LevelManager levelManager;


    QuestManager(LevelManager levelManager) {
        this.levelManager = levelManager;
        this.loadData();
    }


    private void loadData() {
        this.loadChildren();
    }


    private void loadChildren() {
        if(childrenLoaded) {
            return;
        }

        LOGGER.debug(MARKER, "Loading quest children cache...");
        ModRegistries.QUESTS.get().getValues().forEach(quest -> {
            ResourceLocation parentId = quest.getParent();
            ProgressionQuest parentQuest = null;
            if(parentId != null) {
                parentQuest = this.findQuest(parentId);
            }
            else {
                ProgressionLevel level = this.levelManager.getLevelForQuest(quest);
                ResourceLocation previousLevelId = level.getParent();
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
        return Optional.ofNullable(ModRegistries.QUESTS.get().getValue(questId)).orElseThrow(() -> new IllegalArgumentException("Unknown quest with uuid: " + questId));
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
                                   .filter(quest1 -> quest1.getParent() != null && quest1.getParent().equals(id))
                                   .findAny()
                                   .orElse(null);
    }


    public void updateData() {
        this.loadData();
    }


    public Set<ProgressionQuest> searchQuestsForActive(PlayerData player) {
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> isQuestActiveForLevel(progressionQuest, player)).collect(Collectors.toSet());
    }


    public boolean isQuestActiveForLevel(ProgressionQuest quest, PlayerData player) {
        ProgressionLevel level = player.getCurrentLevel();
        return level.contains(quest) && isQuestActive(quest, player);
    }


    public boolean isQuestActive(ProgressionQuest quest, PlayerData player) {
        return quest.isParentDone(this, player) && !quest.isDone(player);
    }


    public Set<ProgressionQuest> searchQuestsForComplete(PlayerData player) {
        return ModRegistries.QUESTS.get().getValues().stream().filter(progressionQuest -> !isQuestActiveForLevel(progressionQuest, player) && progressionQuest.isDone(player)).collect(Collectors.toSet());
    }


    @SuppressWarnings("unchecked")
    public <T> void fireTriggers(Class<? extends SimpleTrigger<T>> triggerClass, PlayerData player, T toTest, Object... data) {
        player.getQuestData().forEachActive(quest -> quest.getCriteria()
                                                 .values()
                                                 .stream()
                                                 .filter(simpleTrigger -> simpleTrigger.getClass().equals(triggerClass))
                                                 .forEach(trigger -> {
                                                     if(!PREventFactory.onTriggerFiring((SimpleTrigger<T>) trigger, player, toTest, data) && ((SimpleTrigger<T>) trigger).trigger(player, toTest, data)) {
                                                         this.updateStatus(player, true);
                                                     }
                                                 })
                          );
    }


    /**
     * Updates the quests of a player depending on whether the quest has been completed or not.
     */
    public void updateStatus(PlayerData player, boolean shouldSyncToTeam) {
        LOGGER.debug(MARKER, "Updating quests for player: {}", player.getName());

        PlayerQuests playerQuests = player.getQuestData();

        playerQuests.updateQuestStatus(this);

        playerQuests.forEachCompleted(quest -> this.unregisterListeners(quest, player));
        playerQuests.forEachActive(quest -> this.registerListeners(quest, player));

        if(shouldSyncToTeam) {
            this.syncQuestProgressToTeam(player);
        }
        this.syncQuestsToClient(player);
        PREventFactory.onQuestStatusUpdate(player);
    }


    public void unregisterListeners(ProgressionQuest quest, PlayerData player) {
        QuestProgress questProgress = player.getQuestData().getOrStartProgress(quest);

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


    public void registerListeners(ProgressionQuest quest, PlayerData player) {
        QuestProgress questProgress = player.getQuestData().getOrStartProgress(quest);
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


    private void syncQuestProgressToTeam(PlayerData player) {
        PlayerDataManager.ensureQuestsSynced(player);
    }


    private void syncQuestsToClient(PlayerData player) {
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(player), player.getServerPlayer());
    }


    public void stopListening(PlayerData player) {
        player.getQuestData().forEachActive(quest -> this.unregisterListeners(quest, player));
    }


    public Collection<QuestProgress> getMainQuestProgress(ProgressionLevel level, PlayerData player) {
        Collection<QuestProgress> questProgresses = new HashSet<>();
        this.getMainQuestsForLevel(level).forEach(quest -> {
            if(quest.isMainQuest()) {
                questProgresses.add(player.getQuestData().getOrStartProgress(quest));
            }
        });
        return questProgresses;
    }


    public Collection<QuestProgress> getSideQuestProgress(ProgressionLevel level, PlayerData player) {
        Collection<QuestProgress> questProgresses = new HashSet<>();
        Collection<ProgressionQuest> levelQuests = level.getQuests()
                                                        .stream()
                                                        .map(this::findQuest)
                                                        .collect(Collectors.toSet());
        levelQuests.forEach(quest -> {
            if(!quest.isMainQuest()) {
                questProgresses.add(player.getQuestData().getOrStartProgress(quest));
            }
        });
        return questProgresses;
    }


    public boolean award(ResourceLocation quest, String criterionName, PlayerData player) {
        return award(findQuest(quest), criterionName, player);
    }


    /**
     * Awards the given criterion to the given quest and if the quest is done, the quest to the given player
     **/
    public boolean award(ProgressionQuest quest, String criterionName, PlayerData player) {
        boolean flag = false;
        QuestProgress questProgress = player.getQuestData().getOrStartProgress(quest);
        if(!PREventFactory.onQuestAward(quest, criterionName, questProgress, player)) {
            boolean flag1 = questProgress.isDone();
            if(criterionName == null || questProgress.grantProgress(criterionName)) {
                if(criterionName == null) {
                    questProgress.complete();
                }
                this.unregisterListeners(quest, player);
                flag = true;
                if(!flag1 && questProgress.isDone()) {
                    quest.rewardPlayer(player); //TODO: Instead of directly rewarding the player, add reward to set --> reward on button press
                    ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(quest.getDisplay(), null), player.getServerPlayer());
                }
            }

            if(flag) {
                this.levelManager.updateStatus();
            }
        }

        return flag;
    }


    public Collection<ProgressionQuest> getMainQuestsForLevel(ProgressionLevel level) {
        return level.getQuests()
                    .stream()
                    .map(this::findQuest)
                    .filter(ProgressionQuest::isMainQuest)
                    .collect(Collectors.toSet());
    }


    public boolean award(ResourceLocation quest, PlayerData player) {
        return award(findQuest(quest), null, player);
    }


    public boolean award(ProgressionQuest quest, PlayerData player) {
        return award(quest, null, player);
    }


    public boolean revoke(ProgressionQuest quest, PlayerData player) {
        return revoke(quest, null, player);
    }


    /**
     * Revokes the given quest for the given player
     **/
    public boolean revoke(ProgressionQuest quest, String criterionName, PlayerData player) {
        boolean flag = false;
        QuestProgress questProgress = player.getQuestData().getOrStartProgress(quest);
        if(!PREventFactory.onQuestRevoke(quest, criterionName, questProgress, player)) {
            if(criterionName == null || questProgress.revokeProgress(criterionName)) {
                if(criterionName == null) {
                    questProgress.reset();
                }
                if(quest.isParentDone(this, player)) {
                    this.registerListeners(quest, player);
                }
                this.levelManager.updateStatus();
                flag = true;
            }
        }

        return flag;
    }


    public boolean revoke(ResourceLocation quest, PlayerData player) {
        return revoke(findQuest(quest), null, player);
    }


    public boolean isParentDone(ProgressionQuest quest, PlayerData player) {
        ProgressionQuest parentQuest = questChildren.inverse().get(quest);
        if(parentQuest != null) {
            return player.getQuestData().getOrStartProgress(parentQuest).isDone();
        }
        return true;
    }


    public boolean hasChild(ProgressionQuest quest) {
        return questChildren.containsKey(quest);
    }
}
