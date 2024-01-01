package de.thedead2.progression_reloaded.data.quest.tasks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class QuestTask {

    private final ResourceLocation id;

    private final Function taskFunction;

    private final QuestStatus questStatus;

    private final QuestCriteria criteria;

    private final Rewards rewards;

    private final Component description;

    private final Set<ResourceLocation> parents = Sets.newHashSet();

    private final Set<ResourceLocation> children = Sets.newHashSet();

    private final boolean optional;


    protected QuestTask(ResourceLocation id, Function taskFunction, QuestStatus questStatus, QuestCriteria criteria, Rewards rewards, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional) {
        this.id = id;
        this.taskFunction = taskFunction;
        this.questStatus = questStatus;
        this.criteria = criteria;
        this.rewards = rewards;
        this.description = description;
        this.parents.addAll(parents);
        this.children.addAll(children);
        this.optional = optional;
    }


    public static QuestTask fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Function taskType = buf.readEnum(Function.class);
        QuestStatus questStatus = buf.readEnum(QuestStatus.class);
        QuestCriteria criteria = QuestCriteria.fromNetwork(buf);
        Rewards rewards = Rewards.fromNetwork(buf);
        Component description = buf.readComponent();
        Set<ResourceLocation> children = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        Set<ResourceLocation> parents = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        boolean optional = buf.readBoolean();

        return new QuestTask(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
    }


    public static QuestTask fromJson(JsonObject jsonObject) {
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Function taskType = Function.valueOf(jsonObject.get("type").getAsString());
        QuestStatus questStatus = QuestStatus.valueOf(jsonObject.get("status").getAsString());
        QuestCriteria criteria = QuestCriteria.fromJson(jsonObject.get("criteria"));
        Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        Set<ResourceLocation> children = SerializationHelper.getNullable(jsonObject, "children", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
        Set<ResourceLocation> parents = SerializationHelper.getNullable(jsonObject, "parents", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
        boolean optional = jsonObject.get("optional").getAsBoolean();

        return new QuestTask(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
    }


    public static QuestTask loadFromNBT(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("id"));
        Function taskType = Function.valueOf(tag.getString("type"));
        QuestStatus questStatus = QuestStatus.valueOf(tag.getString("status"));
        QuestCriteria criteria = QuestCriteria.loadFromNBT(tag.getCompound("criteria"));
        Rewards rewards = Rewards.loadFromNBT(tag.getCompound("rewards"));
        Component description = Component.Serializer.fromJson(tag.getString("description"));
        Set<ResourceLocation> children = SerializationHelper.getNullable(tag, "children", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
        Set<ResourceLocation> parents = SerializationHelper.getNullable(tag, "parents", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
        boolean optional = tag.getBoolean("optional");

        return new QuestTask(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
    }


    public static QuestTask newStartTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> children) {
        return new QuestTask(id, Function.START_TASK, QuestStatus.STARTED, criteria, reward, description, Sets.newHashSet(), children, false);
    }


    public static QuestTask newEndTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> parents, boolean successful) {
        return new QuestTask(id, Function.END_TASK, successful ? QuestStatus.COMPLETE : QuestStatus.FAILED, criteria, reward, description, parents, Sets.newHashSet(), false);
    }


    public static QuestTask newTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional, boolean possibleStartNode) {
        return new QuestTask(id, possibleStartNode ? Function.POSSIBLE_START_TASK : Function.DEFAULT, QuestStatus.ACTIVE, criteria, reward, description, parents, children, optional);
    }


    public static ResourceLocation createId(String id) {
        return new ResourceLocation(ModHelper.MOD_ID, id + "_task");
    }


    public void addChild(QuestTask task) {
        if(this.taskFunction == Function.END_TASK) {
            throw new IllegalStateException("A end action can't have any children!");
        }


        this.children.add(task.id);
        task.addParent(this);
    }


    public void addParent(QuestTask task) {
        if(this.taskFunction == Function.START_TASK) {
            throw new IllegalStateException("A starting action can't have any parents!");
        }

        this.parents.add(task.id);
    }


    public void addChild(ResourceLocation nodeId) {
        if(this.taskFunction == Function.END_TASK) {
            throw new IllegalStateException("An end action can't have any children!");
        }

        this.children.add(nodeId);
    }


    public void addParent(ResourceLocation nodeId) {
        if(this.taskFunction == Function.START_TASK) {
            throw new IllegalStateException("A starting action can't have any parents!");
        }

        this.parents.add(nodeId);
    }


    public boolean hasChildren() {
        return !this.children.isEmpty();
    }


    public Component getDescription() {
        return description;
    }


    public ImmutableSet<ResourceLocation> getParents() {
        return ImmutableSet.copyOf(this.parents);
    }


    public Map<String, SimpleTrigger<?>> getCriteria() {
        return this.criteria.getCriteria();
    }


    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteria.getCriteriaStrategy();
    }


    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", this.id.toString());
        tag.putString("type", this.taskFunction.name());
        tag.putString("status", this.questStatus.name());
        tag.put("criteria", this.criteria.saveToNBT());
        tag.put("rewards", this.rewards.saveToNBT());
        tag.putString("description", Component.Serializer.toJsonTree(this.description).toString());
        SerializationHelper.addNullable(this.children, tag, "children", children -> CollectionHelper.saveToNBT(children, id -> StringTag.valueOf(id.toString())));
        SerializationHelper.addNullable(this.parents, tag, "parents", parents -> CollectionHelper.saveToNBT(parents, id -> StringTag.valueOf(id.toString())));
        tag.putBoolean("optional", this.optional);

        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeEnum(this.taskFunction);
        buf.writeEnum(this.questStatus);
        this.criteria.toNetwork(buf);
        this.rewards.toNetwork(buf);
        buf.writeComponent(this.description);
        buf.writeNullable(this.children, (buf1, children) -> buf1.writeCollection(children, (FriendlyByteBuf::writeResourceLocation)));
        buf.writeNullable(this.parents, (buf1, parents) -> buf1.writeCollection(parents, (FriendlyByteBuf::writeResourceLocation)));
        buf.writeBoolean(this.optional);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", this.id.toString());
        jsonObject.addProperty("type", this.taskFunction.name());
        jsonObject.addProperty("status", this.questStatus.name());
        jsonObject.add("criteria", this.criteria.toJson());
        jsonObject.add("rewards", this.rewards.toJson());
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        SerializationHelper.addNullable(this.children, jsonObject, "children", children -> CollectionHelper.saveToJson(children, id -> new JsonPrimitive(id.toString())));
        SerializationHelper.addNullable(this.parents, jsonObject, "parents", parents -> CollectionHelper.saveToJson(parents, id -> new JsonPrimitive(id.toString())));
        jsonObject.addProperty("optional", this.optional);

        return jsonObject;
    }


    public ImmutableSet<ResourceLocation> getChildren() {
        return ImmutableSet.copyOf(this.children);
    }


    public boolean isOptional() {
        return this.optional;
    }


    public Function getType() {
        return this.taskFunction;
    }


    public Rewards getRewards() {
        return rewards;
    }


    public boolean hasParents() {
        return this.parents.isEmpty();
    }


    public QuestStatus getQuestStatus() {
        return this.questStatus;
    }


    public ResourceLocation getId() {
        return this.id;
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public enum Function {
        START_TASK,
        POSSIBLE_START_TASK,
        DEFAULT,
        END_TASK
    }
}
