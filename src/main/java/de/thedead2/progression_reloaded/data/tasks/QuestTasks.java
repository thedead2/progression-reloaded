package de.thedead2.progression_reloaded.data.tasks;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.IJsonSerializable;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.tasks.types.QuestTask;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;


public class QuestTasks implements INetworkSerializable, IJsonSerializable {

    private final Map<ResourceLocation, QuestTask> tasks = Maps.newHashMap();

    private final Map<QuestTask.Function, Set<QuestTask>> tasksByType = Maps.newHashMap();


    private QuestTasks(Map<ResourceLocation, QuestTask> tasks) {
        this.tasks.putAll(tasks);

        for(var node : this.tasks.values()) {
            QuestTask.Function function = node.getType();
            this.tasksByType.compute(function, (taskFunction, actionNodes) -> (actionNodes == null ? new HashSet<>() : actionNodes)).add(node);
        }
    }


    public static QuestTasks fromNetwork(FriendlyByteBuf buf) {
        Map<ResourceLocation, QuestTask> actions = buf.readMap(Maps::newHashMapWithExpectedSize, FriendlyByteBuf::readResourceLocation, QuestTask::fromNetwork);

        return new QuestTasks(actions);
    }


    public static QuestTasks fromJson(JsonObject jsonObject) {
        Map<ResourceLocation, QuestTask> actions = CollectionHelper.loadFromJson(Maps::newHashMapWithExpectedSize, jsonObject, ResourceLocation::tryParse, jsonElement -> QuestTask.fromJson(jsonElement.getAsJsonObject()));

        return new QuestTasks(actions);
    }


    @NotNull
    public QuestTask getTaskForId(ResourceLocation id) {
        return getTaskForId(this.tasks, id);
    }


    @NotNull
    public static QuestTask getTaskForId(Map<ResourceLocation, QuestTask> nodes, ResourceLocation id) {
        QuestTask task = nodes.get(id);
        if(task == null) {
            throw new IllegalArgumentException("Unknown node for id: " + id);
        }
        return task;
    }


    public ImmutableSet<QuestTask> getTasksByType(QuestTask.Function function) {
        Set<QuestTask> nodes = this.tasksByType.get(function);
        return nodes != null ? ImmutableSet.copyOf(nodes) : ImmutableSet.of();
    }


    public ImmutableMap<ResourceLocation, QuestTask> getTasks() {
        return ImmutableMap.copyOf(this.tasks);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.tasks, FriendlyByteBuf::writeResourceLocation, (buf1, task) -> task.toNetwork(buf1));
    }


    @Override
    public JsonElement toJson() {
        return CollectionHelper.saveToJson(this.tasks, Object::toString, QuestTask::toJson);
    }


    //TODO: Needs revision
    public QuestTask getEndTask(boolean successful) { //TODO: Potential null if no end_task of that type is present
        for(var entry : this.tasks.entrySet()) {
            QuestTask task = entry.getValue();
            if(task.getType() == QuestTask.Function.END_TASK && task.getQuestStatus() == (successful ? QuestStatus.COMPLETE : QuestStatus.FAILED)) {
                return entry.getValue();
            }
        }

        return null;
    }


    public void forEach(BiConsumer<ResourceLocation, QuestTask> consumer) {
        this.tasks.forEach(consumer);
    }


    public Builder deconstruct() {
        Builder builder = Builder.builder();
        builder.tasks.putAll(this.tasks);

        return builder;
    }


    public static class Builder {

        private final Map<ResourceLocation, QuestTask> tasks = new HashMap<>();


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withStartTask(String id, SimpleCriterionTrigger<?> criterion, Rewards rewards, Component description, TaskStrategy strategy, String... children) {
            return this.addTask(QuestTask.newStartTask(QuestTask.createId(id), criterion, rewards, description, CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, QuestTask::createId), strategy));
        }


        private Builder addTask(QuestTask task) {
            this.tasks.put(task.getId(), task);

            return this;
        }


        public Builder withEndTask(String id, SimpleCriterionTrigger<?> criterion, Rewards rewards, Component description, boolean successful) {
            return this.addTask(QuestTask.newEndTask(QuestTask.createId(id), criterion, rewards, description, Sets.newHashSet(), successful));
        }


        public Builder withTask(String id, SimpleCriterionTrigger<?> criterion, Rewards rewards, Component description, boolean optional, boolean possibleStartNode, TaskStrategy strategy, String... children) {
            return this.addTask(QuestTask.newTask(QuestTask.createId(id), criterion, rewards, description, Sets.newHashSet(), CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, QuestTask::createId), strategy, optional, possibleStartNode));
        }


        public QuestTasks build() {
            this.checkTasks();
            return new QuestTasks(this.tasks);
        }


        private void checkTasks() {
            boolean startTask = false, endTask = false;
            for(QuestTask task : this.tasks.values()) {
                QuestTask.Function function = task.getType();
                if(function == QuestTask.Function.START_TASK) {
                    startTask = true;
                }
                else if(function == QuestTask.Function.END_TASK) {
                    endTask = true;
                }

                task.getChildren().forEach(id -> {
                    QuestTask child = this.tasks.get(id);
                    child.addParent(task.getId());
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

}
