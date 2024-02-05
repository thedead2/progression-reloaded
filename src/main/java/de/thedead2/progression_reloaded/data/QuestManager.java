package de.thedead2.progression_reloaded.data;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.misc.HashBiSetMultiMap;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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


    public boolean isQuestActiveForLevel(ProgressionQuest quest, PlayerData player) {
        ProgressionLevel level = player.getCurrentLevel();
        return level.contains(quest) && isQuestActive(quest, player);
    }


    public boolean isQuestActive(ProgressionQuest quest, PlayerData player) {
        return quest.isActive(player);
    }

    /**
     * Updates the quests of a player depending on whether the quest has been completed or not.
     */
    public void updateStatus(PlayerData player) {
        LOGGER.debug(MARKER, "Updating quests for player: {}", player.getName());

        this.getAllQuestsForLevel(player.getCurrentLevel()).forEach(quest -> player.getPlayerQuests().updateQuestStatus(quest));

        PlayerDataManager.ensureQuestsSynced(player);
    }


    public void stopListening(PlayerData player) {
        player.getPlayerQuests().stopListening();
    }


    public Collection<QuestProgress> getMainQuestProgress(ProgressionLevel level, PlayerData player) {
        Collection<QuestProgress> questProgresses = new HashSet<>();
        this.getMainQuestsForLevel(level).forEach(quest -> {
            if(quest.isMainQuest()) {
                questProgresses.add(player.getPlayerQuests().getOrStartProgress(quest));
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
                questProgresses.add(player.getPlayerQuests().getOrStartProgress(quest));
            }
        });
        return questProgresses;
    }


    public Set<ProgressionQuest> getAllQuestsForLevel(ProgressionLevel level) {
        Set<ResourceLocation> questIds = Sets.newHashSet(level.getQuests());

        this.checkParents(level, questIds);

        return questIds.stream()
                       .map(this::findQuest)
                       .collect(Collectors.toSet());
    }


    public boolean award(ResourceLocation quest, boolean successful, PlayerData player) {
        return award(findQuest(quest), successful, player);
    }


    public Collection<ProgressionQuest> getMainQuestsForLevel(ProgressionLevel level) {
        return level.getQuests()
                    .stream()
                    .map(this::findQuest)
                    .filter(ProgressionQuest::isMainQuest)
                    .collect(Collectors.toSet());
    }


    public boolean revoke(ResourceLocation quest, PlayerData player) {
        return revoke(findQuest(quest), player);
    }


    /**
     * Awards the given criterion to the given quest and if the quest is done, the quest to the given player
     **/
    public boolean award(ProgressionQuest quest, boolean successful, PlayerData player) {
        boolean flag = false;
        QuestProgress questProgress = player.getPlayerQuests().getOrStartProgress(quest);
        if(!PREventFactory.onQuestAward(quest, questProgress, player)) {
            if(successful) {
                questProgress.complete();
            }
            else {
                questProgress.fail();
            }
            flag = true;
            this.levelManager.updateStatus();
        }

        return flag;
    }


    public boolean isParentDone(ProgressionQuest quest, PlayerData player) {
        ProgressionQuest parentQuest = questChildren.inverse().get(quest);
        if(parentQuest != null) {
            return player.getPlayerQuests().getOrStartProgress(parentQuest).isDone();
        }
        return true;
    }


    public boolean hasChild(ProgressionQuest quest) {
        return questChildren.containsKey(quest);
    }


    /**
     * Revokes the given quest for the given player
     **/
    public boolean revoke(ProgressionQuest quest, PlayerData player) {
        boolean flag = false;
        QuestProgress questProgress = player.getPlayerQuests().getOrStartProgress(quest);
        QuestStatus oldStatus = questProgress.getCurrentQuestStatus();
        if(!PREventFactory.onQuestRevoke(quest, questProgress, player)) {
            questProgress.reset();
            player.getPlayerQuests().onQuestStatusChanged(quest, oldStatus, questProgress.getCurrentQuestStatus());
            this.levelManager.updateStatus();
            flag = true;
        }

        return flag;
    }


    private void checkParents(ProgressionLevel level, Collection<ResourceLocation> questIds) {
        if(level.hasParent()) {
            ProgressionLevel parent = ModRegistries.LEVELS.get().getValue(level.getParent());
            questIds.addAll(parent.getQuests());
            this.checkParents(parent, questIds);
        }
    }
}
