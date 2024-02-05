package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.tasks.*;
import de.thedead2.progression_reloaded.data.tasks.types.QuestTask;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientOnProgressChangedPacket;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


/**
 * Progress of a quest is dependent on the player or the team. Different players or teams can have different progress of a quest.
 **/

//TODO: Split into QuestProgress and QuestProgressListener
public class QuestProgress implements IProgressInfo<ProgressionQuest> {

    private static final Marker MARKER = new MarkerManager.Log4jMarker("QuestProgressListener");

    private final Supplier<PlayerData> player;

    private final ProgressionQuest quest;

    private final Map<QuestTask, TaskProgress> taskProgress;

    @Nullable
    private QuestTask currentTask;

    private final TaskPath taskPath;


    public QuestProgress(ProgressionQuest quest, Supplier<PlayerData> player) {
        this(player, null, new TaskPath(), quest, Maps.newHashMap());
    }


    private QuestProgress(Supplier<PlayerData> player, @Nullable QuestTask currentTask, TaskPath taskPath, ProgressionQuest quest, Map<QuestTask, TaskProgress> taskProgress) {
        this.player = player;
        this.currentTask = currentTask;
        this.quest = quest;
        this.taskProgress = taskProgress;
        this.taskPath = taskPath;
    }


    public static QuestProgress fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag.getString("quest")));
        QuestTask currentTask = SerializationHelper.getNullable(tag, "currentTask", tag1 -> quest.getTasks().getTaskForId(ResourceLocation.tryParse(tag1.getAsString())));
        TaskPath taskPath = new TaskPath(tag.getString("path"));
        Map<QuestTask, TaskProgress> progress = CollectionHelper.loadFromNBT(tag.getCompound("progress"), s -> quest.getTasks().getTaskForId(ResourceLocation.tryParse(s)), tag1 -> TaskProgress.fromNBT((CompoundTag) tag1));

        return new QuestProgress(() -> PlayerDataManager.getPlayerData(uuid), currentTask, taskPath, quest, progress);
    }


    public static QuestProgress fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(buf.readResourceLocation());
        QuestTask currentTask = buf.readNullable(buf1 -> quest.getTasks().getTaskForId(buf1.readResourceLocation()));
        TaskPath taskPath = new TaskPath(buf.readUtf());
        Map<QuestTask, TaskProgress> nodeProgress = buf.readMap(buf1 -> quest.getTasks().getTaskForId(buf1.readResourceLocation()), TaskProgress::fromNetwork);

        return new QuestProgress(() -> PlayerDataManager.getPlayerData(uuid), currentTask, taskPath, quest, nodeProgress);
    }


    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", this.player.get().getUUID());
        tag.putString("quest", this.quest.getId().toString());
        SerializationHelper.addNullable(this.currentTask, tag, "currentTask", task -> StringTag.valueOf(task.getId().toString()));
        tag.putString("path", this.taskPath.toString());
        tag.put("progress", CollectionHelper.saveToNBT(this.taskProgress, task -> task.getId().toString(), TaskProgress::toNBT));

        return tag;
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.player.get().getUUID());
        buf.writeResourceLocation(this.quest.getId());
        buf.writeNullable(this.currentTask, (buf1, task) -> buf1.writeResourceLocation(task.getId()));
        buf.writeUtf(this.taskPath.toString());
        buf.writeMap(this.taskProgress, (buf1, task) -> buf1.writeResourceLocation(task.getId()), (buf1, taskProgress) -> taskProgress.toNetwork(buf1));
    }


    public void startListening() {
        LOGGER.debug(MARKER, "Started listening for tasks of quest {} for player {}", this.quest.getId(), this.player.get().getName());
        this.registerListeners(this.getCurrentlyListenedTasks(), this.player.get());
    }


    public Set<QuestTask> getCurrentlyListenedTasks() {
        if(this.getCurrentQuestStatus() == QuestStatus.NOT_STARTED || this.currentTask == null) {
            return this.getPotentialStartingTasks();
        }
        else {
            return this.currentTask.getChildren()
                                   .stream()
                                   .map(this::getTaskForId)
                                   .filter(task -> !this.getOrStartProgress(task).isDone())
                                   .collect(Collectors.toSet());
        }
    }


    @NotNull
    public QuestStatus getCurrentQuestStatus() {
        QuestStatus status;
        if(this.currentTask != null) {
            status = this.getOrStartProgress(this.currentTask).getQuestStatus();
        }
        else {
            status = QuestStatus.NOT_STARTED;
        }

        return status;
    }


    @NotNull
    private QuestTask getTaskForId(ResourceLocation id) {
        return this.quest.getTasks().getTaskForId(id);
    }


    public void registerListeners(Collection<QuestTask> tasks, PlayerData player) {
        tasks.forEach(task -> this.registerListeners(task, player));
    }


    public Set<QuestTask> getPotentialStartingTasks() {
        Set<QuestTask> tasks = new HashSet<>();
        QuestTasks questTasks = this.quest.getTasks();
        tasks.addAll(questTasks.getTasksByType(QuestTask.Function.START_TASK));
        tasks.addAll(questTasks.getTasksByType(QuestTask.Function.POSSIBLE_START_TASK));

        return tasks;
    }


    public TaskProgress getOrStartProgress(QuestTask task) {
        TaskProgress taskProgress = this.taskProgress.get(task);
        if(taskProgress == null) {
            taskProgress = new TaskProgress(task);
            this.startProgress(task, taskProgress);
        }
        return taskProgress;
    }


    public Set<QuestTask> getChildrenForCurrentTask() {
        return this.getChildrenForTask(this.currentTask);
    }


    public Set<QuestTask> getChildrenForTask(QuestTask task) {
        Set<QuestTask> children = new HashSet<>();
        if(task != null && task.hasChildren()) {
            task.getChildren().forEach(value -> children.add(this.getTaskForId(value)));
        }

        return children;
    }

    private void registerChildrenListeners(QuestTask task, PlayerData player) {
        task.getChildren().forEach(id -> this.registerListeners(this.getTaskForId(id), player));
    }


    //TODO: Maybe needs revision
    public void registerListeners(QuestTask task, PlayerData player) {
        LOGGER.debug(MARKER, "Registering listeners for task {} for player {}", task.getId(), player.getName());
        TaskProgress taskProgress = this.getOrStartProgress(task);
        if(!taskProgress.isDone()) {
            SimpleCriterionTrigger<?> trigger = task.getCriterion();
            if(trigger != null) {
                if(taskProgress.startTimerIfNeeded()) {
                    player.getPlayerQuests().getTimeKeeper().startListening(new TaskKey(this.quest.getId(), task.getId()), taskProgress.getTimer());
                }

                trigger.addListener(player, new SimpleCriterionTrigger.Listener(quest, task));
            }
        }
    }


    //TODO: Maybe needs revision
    private void startProgress(QuestTask task, TaskProgress taskProgress) {
        this.taskProgress.putIfAbsent(task, taskProgress);
    }


    private TaskStrategy getStrategy() {
        return this.currentTask != null ? this.currentTask.getTaskStrategy() : TaskStrategy.OR;
    }

    /*public boolean revoke(QuestTask task, PlayerData player) {
        LOGGER.debug(MARKER, "Revoking task {} for player {}", task.getId(), player.getName());

        TaskProgress taskProgress = this.getOrStartProgress(task);
        this.previousQuestStatus = this.getCurrentQuestStatus();
        taskProgress.revoke();
        this.registerListeners(task, player);
        LevelManager.getInstance().updateStatus();

        return true;
    }*/


    //TODO: Maybe needs revision
    public void unregisterListeners(QuestTask task, PlayerData player) {
        LOGGER.debug(MARKER, "Unregistering listeners for task {} for player {}", task.getId(), player.getName());
        TaskProgress taskProgress = this.getOrStartProgress(task);

        if(taskProgress.isDone()) {
            SimpleCriterionTrigger<?> criterion = task.getCriterion();
            if(criterion != null) {
                taskProgress.stopTimerIfNeeded();
                player.getPlayerQuests().getTimeKeeper().stopListening(new TaskKey(this.quest.getId(), task.getId()));
                criterion.removeListener(player, new SimpleCriterionTrigger.Listener(quest, task));
            }
        }
    }

    @Override
    public boolean isDone() {
        QuestStatus currentStatus = this.getCurrentQuestStatus();
        return currentStatus == QuestStatus.COMPLETE || currentStatus == QuestStatus.FAILED;
    }


    @Override
    public void reset() {
        LOGGER.debug(MARKER, "Resetting progress for quest {} for player {}", this.quest.getId(), this.player.get().getName());
        this.stopListening();
        this.taskProgress.clear();
        this.setCurrentTask(null);
        this.startListening();
    }


    public void award(QuestTask task, PlayerData player) {
        LOGGER.debug(MARKER, "Awarding task {} for player {}", task.getId(), player.getName());
        this.updateProgress(task, TaskProgress.UpdateMode.COMPLETE);
    }


    @Override
    public void complete() {
        this.awardAllTasks(true);
    }


    public void fail() {
        this.awardAllTasks(false);
    }


    //TODO: Needs revision
    @Override //FIXME: incorrect!
    public float getPercent() {
        if(this.getCurrentQuestStatus() == QuestStatus.NOT_STARTED) {
            return 0f;
        }
        else if(this.isDone()) {
            return 1f;
        }
        else {
            return 0f;
        }
    }


    public QuestTask getEndTask(boolean successful) {
        return this.quest.getTasks().getEndTask(successful);
    }


    private void setCurrentTask(@Nullable QuestTask task) {
        this.currentTask = task;

        if(task != null) {
            this.taskPath.append(task.getId());
        }
        else {
            this.taskPath.clear();
        }
    }


    public void unregisterListeners(Collection<QuestTask> tasks, PlayerData player) {
        tasks.forEach(task -> this.unregisterListeners(task, player));
    }


    @Override
    public ProgressionQuest getProgressable() {
        return this.quest;
    }


    //TODO: Needs revision
    private void awardAllTasks(boolean successful) {
        LOGGER.debug(MARKER, "Force completing quest {} for player {}", this.quest.getId(), this.player.get().getName());
        QuestTasks tasks = this.quest.getTasks();
        QuestStatus status = !successful ? QuestStatus.COMPLETE : QuestStatus.FAILED;

        tasks.forEach((id, task) -> {
            if(task.getQuestStatus() != status) {
                this.award(task, this.player.get());
            }
        });

        this.stopListening();
        this.setCurrentTask(this.getEndTask(successful));
    }


    public void stopListening() {
        LOGGER.debug(MARKER, "Stopped listening for tasks of quest {} for player {}", this.quest.getId(), this.player.get().getName());
        this.unregisterListeners(this.getCurrentlyListenedTasks(), this.player.get());
    }


    public Set<Component> getCurrentDescriptions() {
        Set<Component> set = new HashSet<>();

        this.getCurrentlyListenedTasks().forEach(task -> {
            MutableComponent component = (MutableComponent) task.getDescription();
            TaskProgress taskProgress = this.getOrStartProgress(task);
            component.append(taskProgress.getStatus());

            set.add(component);
        });

        return set;
    }


    //TODO: Needs revision
    public boolean unlock() {
        if(this.getCurrentQuestStatus() != QuestStatus.NOT_STARTED) {
            return false;
        }
        else {
            QuestTask task = this.getPotentialStartingTasks().stream().findFirst().orElseThrow();
            this.award(task, this.player.get());

            return true;
        }
    }


    public void failTask(ResourceLocation taskId) {
        LOGGER.debug("Player {} failed task {}!", this.player.get().getName(), taskId);
        this.updateProgress(this.getTaskForId(taskId), TaskProgress.UpdateMode.NONE);
    }


    public synchronized void updateProgress(@NotNull QuestTask task, TaskProgress.UpdateMode updateMode) {
        PlayerData player = this.player.get();
        TaskProgress taskProgress = this.getOrStartProgress(task);
        TaskStrategy taskStrategy = this.getStrategy();

        boolean counterComplete = switch(updateMode) {
            case NONE -> taskProgress.checkCounter();
            case UPDATE -> taskProgress.updateAndCheckCounter();
            case COMPLETE -> true;
        };
        boolean timerNotFinished = switch(updateMode) {
            case NONE -> taskProgress.checkTimer();
            case UPDATE -> taskProgress.updateAndCheckTimer();
            case COMPLETE -> true;
        };

        QuestStatus previousQuestStatus = this.getCurrentQuestStatus();

        if(!timerNotFinished) {
            taskProgress.fail();

            this.unregisterListeners(task, player);

            if(taskStrategy == TaskStrategy.AND && !task.isOptional()) {
                this.setCurrentTask(task);
                this.stopListening();
            }
        }
        else if(counterComplete) {
            taskProgress.grant();

            this.unregisterListeners(task, player);
            task.rewardPlayer(player);

            switch(taskStrategy) {
                case OR -> {
                    if(!task.isOptional()) {
                        this.unregisterListeners(this.getCurrentlyListenedTasks(), player);

                        this.setCurrentTask(task);
                        this.registerChildrenListeners(task, player);
                    }
                }
                case AND -> {
                    if(TaskStrategy.AND.isDone(this.getCurrentlyListenedTasks().stream().map(this::getOrStartProgress).collect(Collectors.toSet()))) {
                        Set<ResourceLocation> children = new HashSet<>();
                        this.getChildrenForCurrentTask().forEach(task1 -> children.addAll(task1.getChildren()));

                        this.setCurrentTask(task);
                        if(!this.isDone()) {
                            this.registerListeners(children.stream().map(this::getTaskForId).collect(Collectors.toSet()), player);
                        }
                    }
                }
            }
        }

        PREventFactory.onQuestProgressChanged(this.quest, this, player);
        PlayerDataManager.ensureQuestsSynced(player);
        PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(player), player.getServerPlayer());

        ClientOnProgressChangedPacket.Type toastType = ClientOnProgressChangedPacket.Type.QUEST_UPDATED;

        if(previousQuestStatus != this.getCurrentQuestStatus()) {
            if(previousQuestStatus == QuestStatus.NOT_STARTED) {
                toastType = ClientOnProgressChangedPacket.Type.NEW_QUEST;
            }
            else {
                switch(this.getCurrentQuestStatus()) {
                    case COMPLETE -> toastType = ClientOnProgressChangedPacket.Type.QUEST_COMPLETE;
                    case FAILED -> toastType = ClientOnProgressChangedPacket.Type.QUEST_FAILED;
                }
            }

            player.getPlayerQuests().onQuestStatusChanged(this.quest, previousQuestStatus, this.getCurrentQuestStatus());
        }

        PRNetworkHandler.sendToPlayer(new ClientOnProgressChangedPacket(this.quest.getDisplay(), toastType), player.getServerPlayer());

        if(this.isDone()) {
            PREventFactory.onQuestFinished(this.quest, task.getQuestStatus(), player);
            LevelManager.getInstance().updateStatus();
        }
    }
}
