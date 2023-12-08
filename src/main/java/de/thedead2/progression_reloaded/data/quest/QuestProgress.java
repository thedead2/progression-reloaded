package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientDisplayProgressToast;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


/**
 * Progress of a quest is dependent on the player or the team. Different players or teams can have different progress of a quest.
 **/
public class QuestProgress implements IProgressInfo<ProgressionQuest> {

    private static final Marker MARKER = new MarkerManager.Log4jMarker("QuestProgressListener");

    private final Supplier<PlayerData> player;

    private final ProgressionQuest quest;

    private final Map<QuestTasks.Task, QuestTasks.TaskProgress> taskProgress;

    private QuestStatus previousQuestStatus;

    @Nullable
    private QuestTasks.Task currentTask;


    public QuestProgress(ProgressionQuest quest, Supplier<PlayerData> player) {
        this(player, null, quest, Maps.newHashMap());
    }


    private QuestProgress(Supplier<PlayerData> player, @Nullable QuestTasks.Task currentTask, ProgressionQuest quest, Map<QuestTasks.Task, QuestTasks.TaskProgress> taskProgress) {
        this.player = player;
        this.currentTask = currentTask;
        this.quest = quest;
        this.taskProgress = taskProgress;
    }


    public static QuestProgress fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag.getString("quest")));
        QuestTasks.Task currentTask = SerializationHelper.getNullable(tag, "currentTask", tag1 -> quest.getTasks().getTaskForId(ResourceLocation.tryParse(tag1.getAsString())));
        Map<QuestTasks.Task, QuestTasks.TaskProgress> nodeProgress = CollectionHelper.loadFromNBT(tag.getCompound("nodeProgress"), s -> quest.getTasks().getTaskForId(ResourceLocation.tryParse(s)), tag1 -> QuestTasks.TaskProgress.loadFromNBT((CompoundTag) tag1));

        return new QuestProgress(() -> PlayerDataManager.getPlayerData(uuid), currentTask, quest, nodeProgress);
    }


    public static QuestProgress fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(buf.readResourceLocation());
        QuestTasks.Task currentTask = buf.readNullable(buf1 -> quest.getTasks().getTaskForId(buf1.readResourceLocation()));
        Map<QuestTasks.Task, QuestTasks.TaskProgress> nodeProgress = buf.readMap(buf1 -> quest.getTasks().getTaskForId(buf1.readResourceLocation()), QuestTasks.TaskProgress::fromNetwork);

        return new QuestProgress(() -> PlayerDataManager.getPlayerData(uuid), currentTask, quest, nodeProgress);
    }


    public void startListening() {
        LOGGER.debug(MARKER, "Started listening for tasks of quest {} for player {}", this.quest.getId(), this.player.get().getName());
        if(this.currentTask != null) {
            this.currentTask.getChildren().forEach(id -> this.registerListeners(this.getTaskForId(id), this.player.get()));
        }
        else {
            this.registerListeners(this.getPotentialStartingTasks(), this.player.get());
        }
    }


    public Set<QuestTasks.Task> getChildrenForCurrentTask() {
        Set<QuestTasks.Task> children = new HashSet<>();
        if(this.currentTask != null && this.currentTask.hasChildren()) {
            this.currentTask.getChildren().forEach(value -> children.add(this.getTaskForId(value)));
        }

        return children;
    }


    @NotNull
    private QuestTasks.Task getTaskForId(ResourceLocation id) {
        return this.quest.getTasks().getTaskForId(id);
    }


    public void registerListeners(Collection<QuestTasks.Task> tasks, PlayerData player) {
        tasks.forEach(task -> this.registerListeners(task, player));
    }


    public boolean award(QuestTasks.Task task, String criterionName, PlayerData player) {
        LOGGER.debug(MARKER, "Awarding task {} criterion {} for player {}", task.getId(), criterionName, player.getName());
        boolean flag = false;
        boolean questComplete = false;
        QuestTasks.TaskProgress taskProgress = this.getOrStartProgress(task);
        this.previousQuestStatus = this.getCurrentQuestStatus();
        if(criterionName == null || taskProgress.grantProgress(criterionName)) {
            if(criterionName == null) {
                taskProgress.complete();
            }
            if(taskProgress.isDone()) {
                switch(task.getQuestStatus()) {
                    case STARTED: {
                        this.unregisterListeners(this.getPotentialStartingTasks(), player);
                        this.registerChildrenListeners(task, player);

                        ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(this.quest.getDisplay(), Component.literal("New Quest!")), player.getServerPlayer());
                        break;
                    }
                    case ACTIVE: {
                        this.unregisterListeners(task, player);
                        if(!task.isOptional() && this.currentTask != null) {
                            this.currentTask.getChildren().forEach(id -> this.unregisterListeners(this.getTaskForId(id), player));
                        }
                        this.registerChildrenListeners(task, player);

                        if(this.previousQuestStatus != QuestStatus.NOT_STARTED) {
                            ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(this.quest.getDisplay(), Component.literal("Quest updated!")), player.getServerPlayer());
                        }
                        else {
                            ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(this.quest.getDisplay(), Component.literal("New Quest!")), player.getServerPlayer());
                        }
                        break;
                    }
                    case COMPLETE:
                    case FAILED: {
                        this.stopListening();

                        questComplete = true;
                        break;
                    }
                }

                this.currentTask = task;
                this.currentTask.rewardPlayer(player);

                flag = true;
            }
        }

        PREventFactory.onQuestProgressChanged(this.quest, this, player);
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(player), player.getServerPlayer());
        PlayerDataManager.ensureQuestsSynced(player);

        if(this.previousQuestStatus != this.getCurrentQuestStatus()) {
            player.getQuestData().onQuestStatusChanged(this.quest, this.previousQuestStatus, this.getCurrentQuestStatus());
        }

        if(questComplete) {
            ModNetworkHandler.sendToPlayer(new ClientDisplayProgressToast(this.quest.getDisplay(), Component.literal("Quest complete!")), player.getServerPlayer());
            PREventFactory.onQuestComplete(this.quest, task.getQuestStatus(), player);
            LevelManager.getInstance().updateStatus();
        }

        return flag;
    }


    private void registerChildrenListeners(QuestTasks.Task task, PlayerData player) {
        task.getChildren().forEach(id -> this.registerListeners(this.getTaskForId(id), player));
    }


    public void unregisterListeners(QuestTasks.Task task, PlayerData player) {
        LOGGER.debug(MARKER, "Unregistering listeners for task {} for player {}", task.getId(), player.getName());
        QuestTasks.TaskProgress taskProgress = this.getOrStartProgress(task);

        for(Map.Entry<String, SimpleTrigger<?>> entry : task.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = taskProgress.getCriterion(entry.getKey());
            if(criterionprogress != null && (criterionprogress.isDone() || taskProgress.isDone())) {
                SimpleTrigger<?> trigger = entry.getValue();
                if(trigger != null) {
                    trigger.removeListener(player, new SimpleTrigger.Listener(quest, task, entry.getKey()));
                }
            }
        }
    }


    public boolean revoke(QuestTasks.Task task, String criterionName, PlayerData player) {
        LOGGER.debug(MARKER, "Revoking task {} criterion {} for player {}", task.getId(), criterionName, player.getName());
        boolean flag = false;
        QuestTasks.TaskProgress taskProgress = this.getOrStartProgress(task);
        this.previousQuestStatus = this.getCurrentQuestStatus();
        if(criterionName == null || taskProgress.revokeProgress(criterionName)) {
            if(criterionName == null) {
                taskProgress.reset();
            }
            this.registerListeners(task, player);
            flag = true;
            LevelManager.getInstance().updateStatus();
        }

        return flag;
    }


    public QuestTasks.TaskProgress getOrStartProgress(QuestTasks.Task task) {
        QuestTasks.TaskProgress taskProgress = this.taskProgress.get(task);
        if(taskProgress == null) {
            taskProgress = new QuestTasks.TaskProgress(task);
            this.startProgress(task, taskProgress);
        }
        return taskProgress;
    }


    @NotNull
    public QuestStatus getCurrentQuestStatus() {
        QuestStatus status;
        if(this.currentTask != null) {
            status = this.currentTask.getQuestStatus();
        }
        else {
            status = QuestStatus.NOT_STARTED;
        }

        return status;
    }


    public void registerListeners(QuestTasks.Task task, PlayerData player) {
        LOGGER.debug(MARKER, "Registering listeners for task {} for player {}", task.getId(), player.getName());
        QuestTasks.TaskProgress taskProgress = this.getOrStartProgress(task);
        if(!taskProgress.isDone()) {
            for(Map.Entry<String, SimpleTrigger<?>> entry : task.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = taskProgress.getCriterion(entry.getKey());
                if(criterionprogress != null && !criterionprogress.isDone()) {
                    SimpleTrigger<?> trigger = entry.getValue();
                    if(trigger != null) {
                        trigger.addListener(player, new SimpleTrigger.Listener(quest, task, entry.getKey()));
                    }
                }
            }
        }
    }


    private void startProgress(QuestTasks.Task task, QuestTasks.TaskProgress taskProgress) {
        taskProgress.updateProgress(task);
        this.taskProgress.putIfAbsent(task, taskProgress);
    }


    @Override //FIXME: incorrect!
    public float getPercent() {
        if(this.getCurrentQuestStatus() == QuestStatus.NOT_STARTED) {
            return 0f;
        }
        else if(this.isDone()) {
            return 1f;
        }
        else {
            float percent = 0f;

            int counter = 0;
            for(QuestTasks.Task task : Set.copyOf(this.taskProgress.keySet())) {
                if(task.getType() != QuestTasks.Task.Type.START_TASK) {
                    percent += this.getOrStartProgress(task).getPercent();
                    counter++;
                }
            }

            return percent / counter;
        }
    }


    @Override
    public boolean isDone() {
        QuestStatus currentStatus = this.getCurrentQuestStatus();
        return currentStatus == QuestStatus.COMPLETE || currentStatus == QuestStatus.FAILED;
    }


    @Override
    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", this.player.get().getUUID());
        tag.putString("quest", this.quest.getId().toString());
        SerializationHelper.addNullable(this.currentTask, tag, "currentTask", task -> StringTag.valueOf(task.getId().toString()));
        tag.put("progress", CollectionHelper.saveToNBT(this.taskProgress, task -> task.getId().toString(), QuestTasks.TaskProgress::saveToNBT));

        return tag;
    }


    @Override
    public void reset() {
        LOGGER.debug(MARKER, "Resetting progress for quest {} for player {}", this.quest.getId(), this.player.get().getName());
        this.stopListening();
        this.taskProgress.clear();
        this.currentTask = null;
        this.startListening();
    }


    @Override
    public void complete() {
        this.awardAllTasks(true);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.player.get().getUUID());
        buf.writeResourceLocation(this.quest.getId());
        buf.writeNullable(this.currentTask, (buf1, task) -> buf1.writeResourceLocation(task.getId()));
        buf.writeMap(this.taskProgress, (buf1, task) -> buf1.writeResourceLocation(task.getId()), (buf1, taskProgress) -> taskProgress.toNetwork(buf1));
    }


    public void fail() {
        this.awardAllTasks(false);
    }


    private void awardAllTasks(boolean successful) {
        LOGGER.debug(MARKER, "Force completing quest {} for player {}", this.quest.getId(), this.player.get().getName());
        QuestTasks tasks = this.quest.getTasks();
        QuestStatus status = !successful ? QuestStatus.COMPLETE : QuestStatus.FAILED;

        tasks.forEach((id, task) -> {
            if(task.getQuestStatus() != status) {
                this.award(task, null, this.player.get());
            }
        });

        this.stopListening();
        this.currentTask = this.getEndTask(successful);
    }


    public QuestTasks.Task getEndTask(boolean successful) {
        return this.quest.getTasks().getEndTask(successful);
    }


    public void stopListening() {
        LOGGER.debug(MARKER, "Stopped listening for tasks of quest {} for player {}", this.quest.getId(), this.player.get().getName());
        this.unregisterListeners(this.taskProgress.keySet(), this.player.get());
    }


    public void unregisterListeners(Collection<QuestTasks.Task> tasks, PlayerData player) {
        tasks.forEach(task -> this.unregisterListeners(task, player));
    }


    @Override
    public ProgressionQuest getProgressable() {
        return this.quest;
    }


    public Set<Component> getCurrentDescriptions() {
        Set<Component> set = new HashSet<>();

        if(this.getCurrentQuestStatus() == QuestStatus.NOT_STARTED) {
            this.getPotentialStartingTasks()
                .stream()
                .map(QuestTasks.Task::getDescription)
                .forEach(set::add);
        }
        else {
            if(!this.currentTask.hasChildren()) {
                throw new IllegalStateException("The status of quest " + this.quest.getId() + " is active and can not be completed as there are no more actions for completion!");
            }

            this.currentTask
                    .getChildren()
                    .stream()
                    .map(this::getTaskForId)
                    .map(QuestTasks.Task::getDescription)
                    .forEach(set::add);
        }

        return set;
    }


    public Set<QuestTasks.Task> getPotentialStartingTasks() {
        Set<QuestTasks.Task> nodes = new HashSet<>();
        QuestTasks tasks = this.quest.getTasks();
        nodes.addAll(tasks.getTasksByType(QuestTasks.Task.Type.START_TASK));
        nodes.addAll(tasks.getTasksByType(QuestTasks.Task.Type.POSSIBLE_START_TASK));

        return nodes;
    }


    public boolean unlock() {
        if(this.getCurrentQuestStatus() != QuestStatus.NOT_STARTED) {
            return false;
        }
        else {
            QuestTasks.Task task = this.getPotentialStartingTasks().stream().findFirst().orElseThrow();
            return this.award(task, null, this.player.get());
        }
    }
}
