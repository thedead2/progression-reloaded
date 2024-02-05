package de.thedead2.progression_reloaded.data.tasks.types;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.tasks.TaskStrategy;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;


public class QuestTask implements IProgressable<QuestTask> {

    protected final ResourceLocation id;

    protected final Function taskFunction;

    protected final QuestStatus questStatus;

    protected final SimpleCriterionTrigger<?> criterion;

    protected final Rewards rewards;

    protected final Component description;

    protected final Set<ResourceLocation> parents = Sets.newHashSet();

    protected final Set<ResourceLocation> children = Sets.newHashSet();

    protected final TaskStrategy taskStrategy;

    protected final boolean optional;


    protected QuestTask(ResourceLocation id, Function taskFunction, QuestStatus questStatus, SimpleCriterionTrigger<?> criterion, Rewards rewards, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, TaskStrategy taskStrategy, boolean optional) {
        this.id = id;
        this.taskFunction = taskFunction;
        this.questStatus = questStatus;
        this.criterion = criterion;
        this.rewards = rewards;
        this.description = description;
        this.taskStrategy = taskStrategy;
        this.parents.addAll(parents);
        this.children.addAll(children);
        this.optional = optional;
    }


    public static QuestTask fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Function taskType = buf.readEnum(Function.class);
        QuestStatus questStatus = buf.readEnum(QuestStatus.class);
        SimpleCriterionTrigger<?> criterion = SimpleCriterionTrigger.fromNetwork(buf);
        Rewards rewards = Rewards.fromNetwork(buf);
        Component description = buf.readComponent();
        Set<ResourceLocation> children = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        Set<ResourceLocation> parents = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
        TaskStrategy taskStrategy = buf.readEnum(TaskStrategy.class);
        boolean optional = buf.readBoolean();

        return new QuestTask(id, taskType, questStatus, criterion, rewards, description, parents, children, taskStrategy, optional);
    }


    public static QuestTask fromJson(JsonObject jsonObject) {
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Function taskType = Function.valueOf(jsonObject.get("type").getAsString());
        QuestStatus questStatus = QuestStatus.valueOf(jsonObject.get("status").getAsString());
        SimpleCriterionTrigger<?> criterion = SimpleCriterionTrigger.fromJson(jsonObject.get("criterion"));
        Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        Set<ResourceLocation> children = SerializationHelper.getNullable(jsonObject, "children", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
        Set<ResourceLocation> parents = SerializationHelper.getNullable(jsonObject, "parents", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
        TaskStrategy taskStrategy = TaskStrategy.valueOf(jsonObject.get("strategy").getAsString());
        boolean optional = jsonObject.get("optional").getAsBoolean();

        return new QuestTask(id, taskType, questStatus, criterion, rewards, description, parents, children, taskStrategy, optional);
    }


    public static QuestTask newStartTask(ResourceLocation id, SimpleCriterionTrigger<?> criterion, Rewards reward, Component description, Set<ResourceLocation> children, TaskStrategy strategy) {
        return new QuestTask(id, Function.START_TASK, QuestStatus.STARTED, criterion, reward, description, Sets.newHashSet(), children, strategy, false);
    }


    public static QuestTask newEndTask(ResourceLocation id, SimpleCriterionTrigger<?> criterion, Rewards reward, Component description, Set<ResourceLocation> parents, boolean successful) {
        return new QuestTask(id, Function.END_TASK, successful ? QuestStatus.COMPLETE : QuestStatus.FAILED, criterion, reward, description, parents, Sets.newHashSet(), TaskStrategy.OR, false);
    }


    public static QuestTask newTask(ResourceLocation id, SimpleCriterionTrigger<?> criterion, Rewards reward, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, TaskStrategy strategy, boolean optional, boolean possibleStartNode) {
        return new QuestTask(id, possibleStartNode ? Function.POSSIBLE_START_TASK : Function.DEFAULT, QuestStatus.ACTIVE, criterion, reward, description, parents, children, strategy, optional);
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
        MutableComponent component;
        if(this.description == null || this.description.toString().isEmpty()) {
            component = (MutableComponent) this.criterion.getDefaultDescription();
        }
        else {
            component = (MutableComponent) this.description;
        }

        if(this.isOptional()) {
            component.append(" (optional)");
        }

        return component;
    }


    public ImmutableSet<ResourceLocation> getParents() {
        return ImmutableSet.copyOf(this.parents);
    }


    public SimpleCriterionTrigger<?> getCriterion() {
        return this.criterion;
    }


    public TaskStrategy getTaskStrategy() {
        return taskStrategy;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeEnum(this.taskFunction);
        buf.writeEnum(this.questStatus);
        this.criterion.toNetwork(buf);
        this.rewards.toNetwork(buf);
        buf.writeComponent(this.description);
        buf.writeNullable(this.children, (buf1, children) -> buf1.writeCollection(children, (FriendlyByteBuf::writeResourceLocation)));
        buf.writeNullable(this.parents, (buf1, parents) -> buf1.writeCollection(parents, (FriendlyByteBuf::writeResourceLocation)));
        buf.writeEnum(this.taskStrategy);
        buf.writeBoolean(this.optional);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", this.id.toString());
        jsonObject.addProperty("type", this.taskFunction.name());
        jsonObject.addProperty("status", this.questStatus.name());
        jsonObject.add("criterion", this.criterion.toJson());
        jsonObject.add("rewards", this.rewards.toJson());
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        SerializationHelper.addNullable(this.children, jsonObject, "children", children -> CollectionHelper.saveToJson(children, id -> new JsonPrimitive(id.toString())));
        SerializationHelper.addNullable(this.parents, jsonObject, "parents", parents -> CollectionHelper.saveToJson(parents, id -> new JsonPrimitive(id.toString())));
        jsonObject.addProperty("strategy", this.taskStrategy.name());
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


    @Override
    public int compareTo(@NotNull QuestTask o) {
        if(this.id.equals(o.id)) {
            return 0;
        }
        else if(this.hasParents() && this.parents.contains(o.getId())) {
            return 1;
        }
        else if(o.hasParents() && o.parents.contains(this.id)) {
            return -1;
        }
        else {
            throw new IllegalArgumentException("Can't compare task " + this.id + " with task " + o.id + " as they are not related to each other!");
        }
    }


    public enum Function {
        START_TASK,
        POSSIBLE_START_TASK,
        DEFAULT,
        END_TASK
    }
}
