package de.thedead2.progression_reloaded.util.misc;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.tasks.TaskKey;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;


public class TimeKeeper extends Thread {

    private static final Marker MARKER = new MarkerManager.Log4jMarker("TimeKeeper");

    private final Supplier<PlayerQuests> playerQuests;

    private final ConcurrentMap<TaskKey, MinMax.Timer> timers = Maps.newConcurrentMap();

    private boolean keepAlive = true;


    public TimeKeeper(Supplier<PlayerQuests> playerQuests, UUID uuid) {
        super(uuid + "-timekeeper");
        this.playerQuests = playerQuests;
        this.setDaemon(true);
        this.setPriority(2);
        if(ModHelper.isRunningOnServerThread()) {
            ModHelper.LOGGER.debug(MARKER, "Starting timekeeper {}", this.getName());
            this.start();
        }

    }


    public synchronized void startListening(TaskKey taskKey, MinMax.Timer taskTimer) {
        this.timers.put(taskKey, taskTimer);
        ModHelper.LOGGER.debug(MARKER, "Starting to track time for timer of task {}", taskKey.taskId());
        this.notify();
    }


    public synchronized void stopListeningForQuest(ResourceLocation questId) {
        Set<TaskKey> questTasks = new HashSet<>();

        this.timers.keySet().forEach(taskKey -> {
            if(taskKey.questId().equals(questId)) {
                questTasks.add(taskKey);
            }
        });

        questTasks.forEach(this.timers::remove);

        this.notify();
    }


    public synchronized void stopGracefully() {
        this.keepAlive = false;
        this.notify();
    }


    @Override
    public void run() {
        try {
            while(true) {
                synchronized(this) {
                    while(this.timers.isEmpty() && this.keepAlive) {
                        this.wait();
                    }

                    if(!this.keepAlive) {
                        this.timers.clear();
                        ModHelper.LOGGER.debug(MARKER, "Stopping timekeeper {}", this.getName());
                        break;
                    }

                    for(Map.Entry<TaskKey, MinMax.Timer> entry : this.timers.entrySet()) {
                        if(!entry.getValue().updateAndCheck()) {
                            TaskKey taskKey = entry.getKey();
                            PlayerQuests playerQuests = this.playerQuests.get();
                            QuestProgress questProgress = playerQuests.getOrStartProgress(taskKey.questId());

                            questProgress.failTask(taskKey.taskId());
                            this.stopListening(taskKey);
                        }
                    }
                }
            }
        }
        catch(InterruptedException ignored) {
        }
        catch(Throwable e) {
            CrashHandler.getInstance().handleException("An unexpected error occurred while timekeeping", e, Level.ERROR);
        }
    }


    public synchronized void stopListening(TaskKey taskKey) {
        this.timers.remove(taskKey);
        this.notify();
    }
}
