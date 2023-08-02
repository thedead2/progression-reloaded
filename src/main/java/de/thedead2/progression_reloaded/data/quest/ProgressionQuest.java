package de.thedead2.progression_reloaded.data.quest;

import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.registries.ModRegistriesDynamicSerializer;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.rewards.*;
import de.thedead2.progression_reloaded.data.trigger.*;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class ProgressionQuest implements ModRegistriesDynamicSerializer {
    public static ProgressionQuest Test() {
        ProgressionQuest quest = new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test"), Component.literal("Test Quest"), Component.literal("This is a test quest!"), Items.ACACIA_BOAT.getDefaultInstance(), new HashSet<>(), new HashMap<>(), CriteriaStrategy.AND, RewardStrategy.ALL, true, null);
        quest.addTrigger("testSleep", new SleepTrigger(PlayerPredicate.ANY));
        quest.addTrigger("testKill", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.ZOMBIE)));
        quest.addReward(new SpawnEntityReward(EntityType.ALLAY));
        quest.addReward(new ItemReward(Items.DIAMOND.getDefaultInstance(), 25));
        //quest.addCriterion("testTick", new TickTrigger());
        return quest;
    }

    public static ProgressionQuest Test2() {
        ProgressionQuest quest = new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test2"), Component.literal("Test Quest2"), Component.literal("This is a test quest2!"), Items.ACACIA_BOAT.getDefaultInstance(), new HashSet<>(), new HashMap<>(), CriteriaStrategy.OR, RewardStrategy.ALL, false, null);
        quest.addTrigger("testKill", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.HORSE)));
        quest.addReward(new SpawnEntityReward(EntityType.CHICKEN));
        quest.addReward(new ItemReward(Items.EMERALD.getDefaultInstance(), 25));
        //quest.addCriterion("testTick", new TickTrigger());
        return quest;
    }

    public static ProgressionQuest Test3(){
        return new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test3"), Component.literal("Test Quest3"), Component.literal("This is a test quest3!"), Items.ACACIA_BOAT.getDefaultInstance(), Set.of(new CommandReward("/time set midnight")),
                Map.of("test1", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.CHICKEN))), CriteriaStrategy.AND, RewardStrategy.ALL, true, null);
    }
    public static ProgressionQuest Test4(){
        return new ProgressionQuest(new ResourceLocation(ModHelper.MOD_ID, "quest_test4"), Component.literal("Test Quest4"), Component.literal("This is a test quest4!"), Items.ACACIA_BOAT.getDefaultInstance(), Set.of(new TeleportReward(new TeleportReward.TeleportDestination(5, 120, 120, 0, 0, ServerLevel.END))),
                Map.of("test2", new SleepTrigger(PlayerPredicate.ANY)), CriteriaStrategy.AND, RewardStrategy.ALL, true, new ResourceLocation(ModHelper.MOD_ID, "quest_test3"));
    }

    public static ProgressionQuest fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack displayIcon = Item.byId(jsonObject.get("displayIcon").getAsInt()).getDefaultInstance();
        CriteriaStrategy criteriaStrategy1 = CriteriaStrategy.valueOf(jsonObject.get("criteriaStrategy").getAsString());
        RewardStrategy rewardStrategy1 = RewardStrategy.valueOf(jsonObject.get("rewardsStrategy").getAsString());
        boolean mainQuest = jsonObject.get("isMainQuest").getAsBoolean();
        ResourceLocation parent = null;
        if(jsonObject.has("parent")) parent = new ResourceLocation(jsonObject.get("parent").getAsString());

        Set<IReward> questRewards = new HashSet<>();
        jsonObject.get("rewards").getAsJsonArray().forEach(jsonElement1 -> questRewards.add(IReward.createFromJson(jsonElement1)));

        Map<String, SimpleTrigger> questCriteria = new HashMap<>();
        jsonObject.get("criteria").getAsJsonArray().forEach(jsonElement1 -> {
            JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
            questCriteria.put(jsonObject1.get("name").getAsString(), SimpleTrigger.fromJson(jsonObject1.get("trigger")));
        });
        return new ProgressionQuest(id, title, description, displayIcon, questRewards, questCriteria, criteriaStrategy1, rewardStrategy1, mainQuest, parent);
    }

    private void addReward(IReward reward) {
        this.questRewards.add(reward);
    }

    private void addTrigger(String name, SimpleTrigger trigger) {
        this.questCriteria.put(name, trigger);
    }

    private final ResourceLocation id;
    private final Component questTitle;
    private final Component questDescription;
    private final ItemStack displayIcon;
    private final Set<IReward> questRewards;
    private final Map<String, SimpleTrigger> questCriteria;
    private final CriteriaStrategy criteriaStrategy;
    private final RewardStrategy rewardStrategy;
    private final boolean mainQuest;
    private final ResourceLocation parentQuest;
    private final Map<KnownPlayer, Boolean> active = new HashMap<>();

    public ProgressionQuest(ResourceLocation id, Component questTitle, Component questDescription, ItemStack displayIcon, Set<IReward> questRewards, Map<String, SimpleTrigger> questCriteria, CriteriaStrategy criteriaStrategy, RewardStrategy rewardStrategy, boolean mainQuest, ResourceLocation parentQuest) {
        this.id = id;
        this.questTitle = questTitle;
        this.questDescription = questDescription;
        this.displayIcon = displayIcon;
        this.questRewards = questRewards;
        this.questCriteria = questCriteria;
        this.criteriaStrategy = criteriaStrategy;
        this.rewardStrategy = rewardStrategy;
        this.mainQuest = mainQuest;
        this.parentQuest = parentQuest;
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.questTitle));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.questDescription));
        jsonObject.addProperty("displayIcon", Item.getId(this.displayIcon.getItem())); //TODO: better by id!
        jsonObject.addProperty("criteriaStrategy", this.criteriaStrategy.name());
        jsonObject.addProperty("rewardsStrategy", this.rewardStrategy.name());
        jsonObject.addProperty("isMainQuest", this.mainQuest);
        if(this.parentQuest != null) jsonObject.addProperty("parent", this.parentQuest.toString());

        JsonArray jsonArray = new JsonArray();
        this.questRewards.forEach(iReward -> jsonArray.add(iReward.saveToJson()));
        jsonObject.add("rewards", jsonArray);

        JsonArray jsonArray1 = new JsonArray();
        this.questCriteria.forEach((s, trigger) -> {
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("name", s);
            jsonObject1.add("trigger", trigger.toJson());
            jsonArray1.add(jsonObject1);
        });
        jsonObject.add("criteria", jsonArray1);

        return jsonObject;
    }

    public void rewardPlayer(SinglePlayer player){
        this.rewardStrategy.reward(this.questRewards, player);
    }

    public boolean isMainQuest() {
        return mainQuest;
    }

    public ResourceLocation getParentQuest(){
        return this.parentQuest;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressionQuest quest = (ProgressionQuest) o;
        return mainQuest == quest.mainQuest && Objects.equal(id, quest.id) && Objects.equal(questTitle, quest.questTitle) && Objects.equal(questDescription, quest.questDescription) && Objects.equal(displayIcon, quest.displayIcon) && Objects.equal(questRewards, quest.questRewards) && Objects.equal(questCriteria, quest.questCriteria) && criteriaStrategy == quest.criteriaStrategy && rewardStrategy == quest.rewardStrategy && Objects.equal(parentQuest, quest.parentQuest) && Objects.equal(active, quest.active);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, questTitle, questDescription, displayIcon, questRewards, questCriteria, criteriaStrategy, rewardStrategy, mainQuest, parentQuest, active);
    }

    public Map<String, SimpleTrigger> getCriteria() {
        return this.questCriteria;
    }

    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteriaStrategy;
    }

    public boolean isActive(SinglePlayer player) {
        if(this.active.get(KnownPlayer.fromSinglePlayer(player)) == null){
            this.setActive(false, player); //default value for every quest
        }
        return this.active.get(KnownPlayer.fromSinglePlayer(player));
    }

    public void setActive(boolean active, SinglePlayer player) {
        this.active.put(KnownPlayer.fromSinglePlayer(player), active);
    }

    public boolean isParentDone(ActiveQuestManager activeQuestManager, SinglePlayer player) {
        return this.parentQuest == null || activeQuestManager.getOrStartProgress(ModRegistries.QUESTS.get().getValue(this.parentQuest), player).isDone();
    }

    public boolean isDone(ActiveQuestManager activeQuestManager, SinglePlayer player) {
        return activeQuestManager.getOrStartProgress(this, player).isDone();
    }

    public boolean hasParent() {
        return this.parentQuest != null;
    }

    public boolean equalsAny(Collection<ProgressionQuest> quests) {
        boolean flag = false;
        for (ProgressionQuest quest : quests) {
            flag = this.equals(quest);

            if(flag) break;
        }
        return flag;
    }
}
