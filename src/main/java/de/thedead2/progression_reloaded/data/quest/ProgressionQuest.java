package de.thedead2.progression_reloaded.data.quest;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.ICriterion;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.RewardStrategy;
import de.thedead2.progression_reloaded.data.rewards.SpawnEntityReward;
import de.thedead2.progression_reloaded.data.trigger.KillTrigger;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.data.trigger.SleepTrigger;
import de.thedead2.progression_reloaded.data.trigger.TickTrigger;
import de.thedead2.progression_reloaded.player.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class ProgressionQuest {
    public static ProgressionQuest Test() {
        ProgressionQuest quest = new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test"), Component.literal("Test Quest"), Component.literal("This is a test quest!"), Items.ACACIA_BOAT.getDefaultInstance(), CriteriaStrategy.AND, RewardStrategy.ALL, true, null);
        quest.addCriterion("testSleep", new SleepTrigger());
        quest.addCriterion("testKill", new KillTrigger(EntityType.ZOMBIE));
        quest.addReward(new SpawnEntityReward(EntityType.ALLAY));
        quest.addReward(new ItemReward(Items.DIAMOND.getDefaultInstance(), 25));
        //quest.addCriterion("testTick", new TickTrigger());
        return quest;
    }

    public static ProgressionQuest Test2() {
        ProgressionQuest quest = new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test2"), Component.literal("Test Quest2"), Component.literal("This is a test quest2!"), Items.ACACIA_BOAT.getDefaultInstance(), CriteriaStrategy.OR, RewardStrategy.ALL, false, null);
        quest.addCriterion("testKill", new KillTrigger(EntityType.HORSE));
        quest.addReward(new SpawnEntityReward(EntityType.CHICKEN));
        quest.addReward(new ItemReward(Items.EMERALD.getDefaultInstance(), 25));
        //quest.addCriterion("testTick", new TickTrigger());
        return quest;
    }

    private void addReward(IReward reward) {
        this.questRewards.add(reward);
    }

    private void addCriterion(String name, ICriterion criterion) {
        this.questCriteria.put(name, criterion);
    }

    private final ResourceLocation id;
    private final Component questTitle;
    private final Component questDescription;
    private final ItemStack displayIcon;
    private final Set<IReward> questRewards = new HashSet<>();
    private final Map<String, ICriterion> questCriteria = new HashMap<>();
    private final CriteriaStrategy criteriaStrategy;
    private final RewardStrategy rewardStrategy;
    private final boolean mainQuest;
    private final ProgressionQuest parentQuest;
    private final Set<ProgressionQuest> questChildren = new LinkedHashSet<>();
    private boolean active = false;

    public ProgressionQuest(ResourceLocation id, Component questTitle, Component questDescription, ItemStack displayIcon, CriteriaStrategy criteriaStrategy, RewardStrategy rewardStrategy, boolean mainQuest, ProgressionQuest parentQuest) {
        this.id = id;
        this.questTitle = questTitle;
        this.questDescription = questDescription;
        this.displayIcon = displayIcon;
        this.criteriaStrategy = criteriaStrategy;
        this.rewardStrategy = rewardStrategy;
        this.mainQuest = mainQuest;
        this.parentQuest = parentQuest;

        if(this.parentQuest != null){
            this.parentQuest.addChild(this);
        }
    }

    public void addChild(ProgressionQuest quest){
        questChildren.add(quest);
    }

    public void rewardPlayer(SinglePlayer player){
        this.rewardStrategy.reward(this.questRewards, player);
    }

    public boolean isMainQuest() {
        return mainQuest;
    }

    public Set<ProgressionQuest> getChildren() {
        return questChildren;
    }

    public ProgressionQuest getParentQuest(){
        return this.parentQuest;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressionQuest that = (ProgressionQuest) o;
        return mainQuest == that.mainQuest && Objects.equal(id, that.id) && Objects.equal(questTitle, that.questTitle) && Objects.equal(questDescription, that.questDescription) && Objects.equal(displayIcon, that.displayIcon) && Objects.equal(questRewards, that.questRewards) && Objects.equal(questCriteria, that.questCriteria) && criteriaStrategy == that.criteriaStrategy && rewardStrategy == that.rewardStrategy && Objects.equal(parentQuest, that.parentQuest) && Objects.equal(questChildren, that.questChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, questTitle, questDescription, displayIcon, questRewards, questCriteria, criteriaStrategy, rewardStrategy, mainQuest, parentQuest, questChildren);
    }

    public Map<String, ICriterion> getCriteria() {
        return this.questCriteria;
    }

    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteriaStrategy;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isParentDone(ProgressionLevel level) {
        return level.getOrStartProgress(this.parentQuest).isDone();
    }

    public boolean isDone(ProgressionLevel level) {
        return level.getOrStartProgress(this).isDone();
    }

    public boolean hasParent() {
        return this.parentQuest != null;
    }
}
