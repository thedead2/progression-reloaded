package de.thedead2.progression_reloaded.data.level;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.criteria.ICriterion;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.rewards.RewardStrategy;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

public class ProgressionLevel {

    private final int index;
    private final String name;
    private final ResourceLocation id;
    private final Set<IReward> rewards = new HashSet<>();
    private final RewardStrategy rewardStrategy;
    private final List<ProgressionQuest> quests;
    private final Map<ProgressionQuest, QuestProgress> mainQuests = new HashMap<>();
    private final Map<ProgressionQuest, QuestProgress> sideQuests = new HashMap<>();
    @Nullable private final ProgressionLevel previousLevel;
    @Nullable private final ProgressionLevel nextLevel;

    private final SinglePlayer player;
    private final Set<ProgressionQuest> progressChanged = Sets.newLinkedHashSet();

    public ProgressionLevel(int index, String name, ResourceLocation id, RewardStrategy rewardStrategy, List<ProgressionQuest> quests, @Nullable ProgressionLevel previousLevel, @Nullable ProgressionLevel nextLevel, SinglePlayer player) {
        this.index = index;
        this.name = name;
        this.id = id;
        this.rewardStrategy = rewardStrategy;
        this.quests = quests;
        this.previousLevel = previousLevel;
        this.nextLevel = nextLevel;
        this.player = player;
    }

    public ProgressionLevel(String name, ResourceLocation id, SinglePlayer player, RewardStrategy rewardStrategy, ProgressionLevel nextLevel, List<ProgressionQuest> quests) {
        this(0, name, id, rewardStrategy, quests, null, nextLevel, player);
    }

    public static ProgressionLevel fromKey(ResourceLocation level, SinglePlayer player) {
        return lowest(player);
    }

    public static ProgressionLevel lowest(SinglePlayer player) {
        return new ProgressionLevel("base", new ResourceLocation(ModHelper.MOD_ID, "base_level"), player, RewardStrategy.ALL, null, Collections.emptyList());
    }

    public void startListening(){
        this.quests.forEach(this::registerListeners);
    }


    public boolean contains(ProgressionLevel other) {
        if(this.equals(other) || (this.previousLevel != null && previousLevel.equals(other))) return true;
        else if(this.previousLevel == null) return false;
        else return previousLevel.contains(other);
    }

    public ProgressionLevel getPreviousLevel() {
        return previousLevel;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void rewardPlayer() {
        this.rewardStrategy.reward(this.rewards, this.player);
    }

    public void tick() {
        if(this.isDone()){
            this.player.updateProgressionLevel(this.nextLevel);
        }
        else {

        }
    }

    public QuestProgress getOrStartProgress(ProgressionQuest quest) {
        QuestProgress questProgress = this.getCorrectQuestMap(quest).get(quest);
        if (questProgress == null) {
            questProgress = new QuestProgress();
            this.startProgress(quest, questProgress);
        }

        return questProgress;
    }

    private Map<ProgressionQuest, QuestProgress> getCorrectQuestMap(ProgressionQuest quest) {
        if(quest.isMainQuest()) return this.mainQuests;
        else return this.sideQuests;
    }

    private void startProgress(ProgressionQuest quest, QuestProgress questProgress) {
        questProgress.update(quest.getCriteria(), quest.getCriteriaStrategy());
        this.getCorrectQuestMap(quest).put(quest, questProgress);
    }

    private void registerListeners(ProgressionQuest quest) {
        QuestProgress questProgress = this.getOrStartProgress(quest);
        if (!questProgress.isDone()) {
            for(Map.Entry<String, ICriterion> entry : quest.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
                if (criterionprogress != null && !criterionprogress.isDone()) {
                    SimpleTrigger trigger = entry.getValue().getTrigger();
                    if (trigger != null) {
                        trigger.addListener(this, new SimpleTrigger.Listener<>(trigger, quest, entry.getKey()));
                    }
                }
            }

        }
    }

    private void unregisterListeners(ProgressionQuest quest) {
        QuestProgress questProgress = this.getOrStartProgress(quest);

        for(Map.Entry<String, ICriterion> entry : quest.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = questProgress.getCriterion(entry.getKey());
            if (criterionprogress != null && (criterionprogress.isDone() || questProgress.isDone())) {
                SimpleTrigger trigger = entry.getValue().getTrigger();
                if (trigger != null) {
                    trigger.removeListener(this, new SimpleTrigger.Listener<>(trigger, quest, entry.getKey()));
                }
            }
        }

    }

    public boolean award(ProgressionQuest quest, String criterionName) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest);
        boolean flag1 = questProgress.isDone();
        if (questProgress.grantProgress(criterionName)) {
            this.unregisterListeners(quest);
            this.progressChanged.add(quest);
            flag = true;
            if (!flag1 && questProgress.isDone()) {
                quest.rewardPlayer(this.player);
            }
        }

        return flag;
    }

    public boolean revoke(ProgressionQuest quest, String criterionName) {
        boolean flag = false;
        QuestProgress questProgress = this.getOrStartProgress(quest);
        if (questProgress.revokeProgress(criterionName)) {
            this.registerListeners(quest);
            this.progressChanged.add(quest);
            flag = true;
        }

        return flag;
    }

    public boolean isDone() {
        boolean flag = false;
        for(Map.Entry<ProgressionQuest, QuestProgress> entry : this.mainQuests.entrySet()){
            QuestProgress questProgress = entry.getValue();
            if (questProgress != null && questProgress.isDone()) {
                flag = true;
            }
            else {
                flag = false;
                break;
            }
        }

        return flag;
    }

    public float getPercent() {
        if (this.quests.isEmpty()) {
            return 0.0F;
        } else {
            float f = (float) this.quests.size();
            float f1 = (float) this.countCompletedCriteria();
            return f1 / f;
        }
    }

    private int countCompletedCriteria() {
        int i = 0;

        for(ProgressionQuest quest : quests){
            QuestProgress questProgress = this.getCorrectQuestMap(quest).get(quest);
            if (questProgress != null && questProgress.isDone()) {
                i++;
            }
        }

        return i;
    }

    public boolean hasProgress() {
        for(QuestProgress questProgress : this.mainQuests.values()) {
            if (questProgress.isDone()) {
                return true;
            }
        }
        for(QuestProgress questProgress : this.sideQuests.values()) {
            if (questProgress.isDone()) {
                return true;
            }
        }

        return false;
    }

    public void stopListening() {
        this.quests.forEach(this::unregisterListeners);
    }
}
