package de.thedead2.progression_reloaded.data.quest;

import com.google.common.base.Objects;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.QuestManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.JsonHelper;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

public class ProgressionQuest implements ModRegistriesDynamicSerializer {

    public static ProgressionQuest fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack displayIcon = JsonHelper.itemFromJson(jsonObject.get("displayIcon").getAsJsonObject());
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

    public void addReward(IReward reward) {
        this.questRewards.add(reward);
    }

    public void addTrigger(String name, SimpleTrigger trigger) {
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
    //private final Map<KnownPlayer, Boolean> active = new HashMap<>();

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
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOGGER.debug("Quest {}: \n{}", this.questTitle.getString(), gson.toJson(this));*/
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.questTitle));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.questDescription));
        jsonObject.add("displayIcon", JsonHelper.itemToJson(this.displayIcon));
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
        return mainQuest == quest.mainQuest && Objects.equal(id, quest.id) && Objects.equal(questTitle, quest.questTitle) && Objects.equal(questDescription, quest.questDescription) && Objects.equal(displayIcon, quest.displayIcon) && Objects.equal(questRewards, quest.questRewards) && Objects.equal(questCriteria, quest.questCriteria) && criteriaStrategy == quest.criteriaStrategy && rewardStrategy == quest.rewardStrategy && Objects.equal(parentQuest, quest.parentQuest) /*&& Objects.equal(active, quest.active)*/;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, questTitle, questDescription, displayIcon, questRewards, questCriteria, criteriaStrategy, rewardStrategy, mainQuest, parentQuest /*active*/);
    }

    public Map<String, SimpleTrigger> getCriteria() {
        return this.questCriteria;
    }

    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteriaStrategy;
    }

    public boolean isParentDone(QuestManager questManager, KnownPlayer player) {
        if(this.parentQuest == null) {
            ProgressionLevel level = LevelManager.getInstance().getLevelForQuest(this);
            ResourceLocation previousLevelId = level.getPreviousLevel();
            if(previousLevelId != null){
                ProgressionQuest quest = LevelManager.getInstance().getQuestManager().getLastMainQuestForLevel(ModRegistries.LEVELS.get().getValue(previousLevelId));
                if(quest != null) return questManager.getOrStartProgress(quest, player).isDone();
            }
        }
        return this.parentQuest == null || questManager.getOrStartProgress(ModRegistries.QUESTS.get().getValue(this.parentQuest), player).isDone();
    }

    public boolean isDone(QuestManager questManager, KnownPlayer player) {
        return questManager.getOrStartProgress(this, player).isDone();
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

    public boolean hasChild() {
        return LevelManager.getInstance().getQuestManager().findChildQuest(this) != null;
    }
}
