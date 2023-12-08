package de.thedead2.progression_reloaded.data.quest;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;


public class QuestTasks {

    private final Map<ResourceLocation, Task> tasks = Maps.newHashMap();

    private final Map<Task.Type, Set<Task>> tasksByType = Maps.newHashMap();


    private QuestTasks(Map<ResourceLocation, Task> tasks) {
        this.tasks.putAll(tasks);

        for(var node : this.tasks.values()) {
            Task.Type type = node.getType();
            this.tasksByType.compute(type, (taskType, actionNodes) -> (actionNodes == null ? new HashSet<>() : actionNodes)).add(node);
        }
    }


    public static QuestTasks loadFromNBT(CompoundTag tag) {
        Map<ResourceLocation, Task> actions = CollectionHelper.loadFromNBT(Maps::newHashMapWithExpectedSize, tag, ResourceLocation::tryParse, tag1 -> Task.loadFromNBT((CompoundTag) tag1));

        return new QuestTasks(actions);
    }


    public static QuestTasks fromNetwork(FriendlyByteBuf buf) {
        Map<ResourceLocation, Task> actions = buf.readMap(Maps::newHashMapWithExpectedSize, FriendlyByteBuf::readResourceLocation, Task::fromNetwork);

        return new QuestTasks(actions);
    }


    public static QuestTasks fromJson(JsonObject jsonObject) {
        Map<ResourceLocation, Task> actions = CollectionHelper.loadFromJson(Maps::newHashMapWithExpectedSize, jsonObject, ResourceLocation::tryParse, jsonElement -> Task.fromJson(jsonElement.getAsJsonObject()));

        return new QuestTasks(actions);
    }


    @NotNull
    public QuestTasks.Task getTaskForId(ResourceLocation id) {
        return getTaskForId(this.tasks, id);
    }


    @NotNull
    public static QuestTasks.Task getTaskForId(Map<ResourceLocation, Task> nodes, ResourceLocation id) {
        Task task = nodes.get(id);
        if(task == null) {
            throw new IllegalArgumentException("Unknown node for id: " + id);
        }
        return task;
    }


    public ImmutableSet<Task> getTasksByType(Task.Type type) {
        Set<Task> nodes = this.tasksByType.get(type);
        return nodes != null ? ImmutableSet.copyOf(nodes) : ImmutableSet.of();
    }


    public @NotNull CompoundTag saveToNBT() {
        return CollectionHelper.saveToNBT(this.tasks, Object::toString, Task::saveToNBT);
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.tasks, FriendlyByteBuf::writeResourceLocation, (buf1, task) -> task.toNetwork(buf1));
    }


    public JsonElement toJson() {
        return CollectionHelper.saveToJson(this.tasks, Object::toString, Task::toJson);
    }


    public Task getEndTask(boolean successful) { //TODO: Potential null if no end_task of that type is present
        for(var entry : this.tasks.entrySet()) {
            Task task = entry.getValue();
            if(task.getType() == Task.Type.END_TASK && task.getQuestStatus() == (successful ? QuestStatus.COMPLETE : QuestStatus.FAILED)) {
                return entry.getValue();
            }
        }

        return null;
    }


    public void forEach(BiConsumer<ResourceLocation, Task> consumer) {
        this.tasks.forEach(consumer);
    }


    public static class Builder {

        private final Map<ResourceLocation, Task> tasks = new HashMap<>();


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withStartTask(String id, QuestCriteria criteria, Rewards rewards, Component description, String... children) {
            return this.addTask(Task.newStartTask(Task.createId(id), criteria, rewards, description, CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, Task::createId)));
        }


        private Builder addTask(Task task) {
            this.tasks.put(task.id, task);

            return this;
        }


        public Builder withEndTask(String id, QuestCriteria criteria, Rewards rewards, Component description, boolean successful) {
            return this.addTask(Task.newEndTask(Task.createId(id), criteria, rewards, description, Sets.newHashSet(), successful));
        }


        public Builder withTask(String id, QuestCriteria criteria, Rewards rewards, Component description, boolean optional, boolean possibleStartNode, String... children) {
            return this.addTask(Task.newTask(Task.createId(id), criteria, rewards, description, Sets.newHashSet(), CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, Task::createId), optional, possibleStartNode));
        }


        public QuestTasks build() {
            this.checkTasks();
            return new QuestTasks(this.tasks);
        }


        private void checkTasks() {
            boolean startTask = false, endTask = false;
            for(Task task : this.tasks.values()) {
                Task.Type type = task.getType();
                if(type == Task.Type.START_TASK) {
                    startTask = true;
                }
                else if(type == Task.Type.END_TASK) {
                    endTask = true;
                }

                task.getChildren().forEach(id -> {
                    Task child = this.tasks.get(id);
                    child.addParent(task.id);
                });
            }

            if(!startTask) {
                throw new IllegalStateException("Missing start task! Each quest needs to have at least one start task!");
            }
            if(!endTask) {
                throw new IllegalStateException("Missing end task! Each quest needs to have at least one end task!");
            }
        }
    }

    public static class Task {

        private final ResourceLocation id;

        private final Type taskType;

        private final QuestStatus questStatus;

        private final QuestCriteria criteria;

        private final Rewards rewards;

        private final Component description;

        private final Set<ResourceLocation> parents = Sets.newHashSet();

        private final Set<ResourceLocation> children = Sets.newHashSet();

        private final boolean optional;


        private Task(ResourceLocation id, Type taskType, QuestStatus questStatus, QuestCriteria criteria, Rewards rewards, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional) {
            this.id = id;
            this.taskType = taskType;
            this.questStatus = questStatus;
            this.criteria = criteria;
            this.rewards = rewards;
            this.description = description;
            this.parents.addAll(parents);
            this.children.addAll(children);
            this.optional = optional;
        }


        public static Task newStartTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> children) {
            return new Task(id, Type.START_TASK, QuestStatus.STARTED, criteria, reward, description, Sets.newHashSet(), children, false);
        }


        public static Task newEndTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> parents, boolean successful) {
            return new Task(id, Type.END_TASK, successful ? QuestStatus.COMPLETE : QuestStatus.FAILED, criteria, reward, description, parents, Sets.newHashSet(), false);
        }


        public static Task newTask(ResourceLocation id, QuestCriteria criteria, Rewards reward, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional, boolean possibleStartNode) {
            return new Task(id, possibleStartNode ? Type.POSSIBLE_START_TASK : Type.DEFAULT, QuestStatus.ACTIVE, criteria, reward, description, parents, children, optional);
        }


        public static ResourceLocation createId(String id) {
            return new ResourceLocation(ModHelper.MOD_ID, id + "_task");
        }


        public static Task fromNetwork(FriendlyByteBuf buf) {
            ResourceLocation id = buf.readResourceLocation();
            Type taskType = buf.readEnum(Type.class);
            QuestStatus questStatus = buf.readEnum(QuestStatus.class);
            QuestCriteria criteria = QuestCriteria.fromNetwork(buf);
            Rewards rewards = Rewards.fromNetwork(buf);
            Component description = buf.readComponent();
            Set<ResourceLocation> children = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
            Set<ResourceLocation> parents = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
            boolean optional = buf.readBoolean();

            return new Task(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
        }


        public static Task fromJson(JsonObject jsonObject) {
            ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
            Type taskType = Type.valueOf(jsonObject.get("type").getAsString());
            QuestStatus questStatus = QuestStatus.valueOf(jsonObject.get("status").getAsString());
            QuestCriteria criteria = QuestCriteria.fromJson(jsonObject.get("criteria"));
            Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));
            Component description = Component.Serializer.fromJson(jsonObject.get("description"));
            Set<ResourceLocation> children = SerializationHelper.getNullable(jsonObject, "children", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
            Set<ResourceLocation> parents = SerializationHelper.getNullable(jsonObject, "parents", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
            boolean optional = jsonObject.get("optional").getAsBoolean();

            return new Task(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
        }


        public static Task loadFromNBT(CompoundTag tag) {
            ResourceLocation id = new ResourceLocation(tag.getString("id"));
            Type taskType = Type.valueOf(tag.getString("type"));
            QuestStatus questStatus = QuestStatus.valueOf(tag.getString("status"));
            QuestCriteria criteria = QuestCriteria.loadFromNBT(tag.getCompound("criteria"));
            Rewards rewards = Rewards.loadFromNBT(tag.getCompound("rewards"));
            Component description = Component.Serializer.fromJson(tag.getString("description"));
            Set<ResourceLocation> children = SerializationHelper.getNullable(tag, "children", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
            Set<ResourceLocation> parents = SerializationHelper.getNullable(tag, "parents", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
            boolean optional = tag.getBoolean("optional");

            return new Task(id, taskType, questStatus, criteria, rewards, description, parents, children, optional);
        }


        public void addChild(Task task) {
            if(this.taskType == Type.END_TASK) {
                throw new IllegalStateException("A end action can't have any children!");
            }


            this.children.add(task.id);
            task.addParent(this);
        }


        public void addParent(Task task) {
            if(this.taskType == Type.START_TASK) {
                throw new IllegalStateException("A starting action can't have any parents!");
            }

            this.parents.add(task.id);
        }


        public void addChild(ResourceLocation nodeId) {
            if(this.taskType == Type.END_TASK) {
                throw new IllegalStateException("An end action can't have any children!");
            }

            this.children.add(nodeId);
        }


        public void addParent(ResourceLocation nodeId) {
            if(this.taskType == Type.START_TASK) {
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
            tag.putString("type", this.taskType.name());
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
            buf.writeEnum(this.taskType);
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
            jsonObject.addProperty("type", this.taskType.name());
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


        public Type getType() {
            return this.taskType;
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


        public enum Type {
            START_TASK,
            POSSIBLE_START_TASK,
            DEFAULT,
            END_TASK
        }
    }

    public static class TaskProgress implements Comparable<TaskProgress> {

        private final Map<String, CriterionProgress> criteria;

        private final CriteriaStrategy criteriaStrategy;


        public TaskProgress(Task task) {
            this(Maps.newHashMap(), task.getCriteriaStrategy());
        }


        private TaskProgress(Map<String, CriterionProgress> criteria, CriteriaStrategy strategy) {
            this.criteria = criteria;
            this.criteriaStrategy = strategy;
        }


        public static TaskProgress loadFromNBT(CompoundTag tag) {
            Map<String, CriterionProgress> criteria = CollectionHelper.loadFromNBT(tag.getCompound("criteria"), s -> s, tag1 -> CriterionProgress.loadFromNBT((CompoundTag) tag1));
            CriteriaStrategy strategy = CriteriaStrategy.valueOf(tag.getString("strategy"));

            return new TaskProgress(criteria, strategy);
        }


        public static TaskProgress fromNetwork(FriendlyByteBuf buf) {
            Map<String, CriterionProgress> map = buf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
            CriteriaStrategy strategy = buf.readEnum(CriteriaStrategy.class);
            return new TaskProgress(map, strategy);
        }


        public static TaskProgress fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Map<String, CriterionProgress> criteria = CollectionHelper.loadFromJson(jsonObject.getAsJsonObject("criteria"), s -> s, CriterionProgress::fromJson);
            CriteriaStrategy strategy = CriteriaStrategy.valueOf(jsonObject.get("strategy").getAsString());

            return new TaskProgress(criteria, strategy);
        }


        public boolean isDone() {
            return this.criteriaStrategy.isDone(this);
        }


        public boolean hasProgress() {
            for(CriterionProgress criterionprogress : this.criteria.values()) {
                if(criterionprogress.isDone()) {
                    return true;
                }
            }

            return false;
        }


        public void updateProgress(Task task) {
            Set<String> set = task.getCriteria().keySet();
            this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

            for(String s : set) {
                if(!this.criteria.containsKey(s)) {
                    this.criteria.put(s, new CriterionProgress());
                }
            }
        }


        public float getPercent() {
            if(this.criteria.isEmpty()) {
                return 0.0F;
            }
            else {
                int f = this.criteria.size();
                int f1 = this.countCompletedCriteria();
                return (float) f1 / f;
            }
        }


        private int countCompletedCriteria() {
            int i = 0;

            for(String s : criteria.keySet()) {
                CriterionProgress criterionprogress = this.getCriterion(s);
                if(criterionprogress != null && criterionprogress.isDone()) {
                    i++;
                }
            }

            return i;
        }


        @Nullable
        public CriterionProgress getCriterion(String criterionName) {
            return this.criteria.get(criterionName);
        }


        public Iterable<String> getRemainingCriteria() {
            List<String> list = Lists.newArrayList();

            for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
                if(!entry.getValue().isDone()) {
                    list.add(entry.getKey());
                }
            }

            return list;
        }


        public Iterable<String> getCompletedCriteria() {
            List<String> list = Lists.newArrayList();

            for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
                if(entry.getValue().isDone()) {
                    list.add(entry.getKey());
                }
            }

            return list;
        }


        public int compareTo(TaskProgress taskProgress) {
            Date date = this.getFirstProgressDate();
            Date date1 = taskProgress.getFirstProgressDate();
            if(date == null && date1 != null) {
                return 1;
            }
            else if(date != null && date1 == null) {
                return -1;
            }
            else {
                return date == null ? 0 : date.compareTo(date1);
            }
        }


        @Nullable
        public Date getFirstProgressDate() {
            Date date = null;

            for(CriterionProgress criterionprogress : this.criteria.values()) {
                if(criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
                    date = criterionprogress.getObtained();
                }
            }

            return date;
        }


        public CompoundTag saveToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("criteria", CollectionHelper.saveToNBT(this.criteria, s -> s, CriterionProgress::saveToCompoundTag));
            tag.putString("strategy", this.criteriaStrategy.name());
            return tag;
        }


        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, criterionProgress) -> criterionProgress.toNetwork(buf1));
            buf.writeEnum(this.criteriaStrategy);
        }


        public void reset() {
            this.criteria.forEach((s, criterionProgress) -> this.revokeProgress(s));
        }


        /**
         * Revokes the given criterion for this progress
         **/
        public boolean revokeProgress(String criterionName) {
            CriterionProgress criterionprogress = this.criteria.get(criterionName);
            if(criterionprogress != null && criterionprogress.isDone()) {
                criterionprogress.revoke();
                return true;
            }
            else {
                return false;
            }
        }


        public Map<String, CriterionProgress> getCriteria() {
            return criteria;
        }


        public void complete() {
            this.criteria.forEach((s, criterionProgress) -> this.grantProgress(s));
        }


        /**
         * Grants the given criterion to this progress
         **/
        public boolean grantProgress(String criterionName) {
            CriterionProgress criterionprogress = this.criteria.get(criterionName);
            if(criterionprogress != null && !criterionprogress.isDone()) {
                criterionprogress.grant();
                return true;
            }
            else {
                return false;
            }
        }


        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("criteria", CollectionHelper.saveToJson(this.criteria, s -> s, CriterionProgress::serializeToJson));
            jsonObject.addProperty("strategy", this.criteriaStrategy.name());

            return jsonObject;
        }
    }
}
