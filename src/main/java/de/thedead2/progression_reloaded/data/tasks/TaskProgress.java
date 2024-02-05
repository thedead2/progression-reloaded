package de.thedead2.progression_reloaded.data.tasks;

import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.tasks.types.QuestTask;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TaskProgress {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);

    private final boolean optional;

    private final MinMax.Counter counter;

    private final MinMax.Timer timer;

    @Nullable
    private Date obtained;

    private QuestStatus questStatus;


    public TaskProgress(QuestTask task) {
        this(new MinMax.Counter(task.getCriterion().getAmount()), new MinMax.Timer(task.getCriterion().getDuration()), task.getQuestStatus(), null, task.isOptional());
    }


    private TaskProgress(MinMax.Counter counter, MinMax.Timer timer, QuestStatus questStatus, @Nullable Date obtained, boolean optional) {
        this.counter = counter;
        this.timer = timer;
        this.questStatus = questStatus;
        this.obtained = obtained;
        this.optional = optional;
    }


    public static TaskProgress fromNetwork(FriendlyByteBuf buf) {
        MinMax.Counter counter = MinMax.Counter.fromNetwork(buf);
        MinMax.Timer timer = MinMax.Timer.fromNetwork(buf);
        QuestStatus questStatus = buf.readEnum(QuestStatus.class);
        Date obtained = buf.readNullable(FriendlyByteBuf::readDate);
        boolean optional = buf.readBoolean();

        return new TaskProgress(counter, timer, questStatus, obtained, optional);
    }


    public static TaskProgress fromNBT(CompoundTag tag) {
        MinMax.Counter counter = MinMax.Counter.fromNBT(tag.getCompound("counter"));
        MinMax.Timer timer = MinMax.Timer.fromNBT(tag.getCompound("timer"));
        QuestStatus questStatus = QuestStatus.valueOf(tag.getString("status"));
        Date obtained = SerializationHelper.getNullable(tag, "obtained", tag1 -> {
            try {
                return DATE_FORMAT.parse(tag.getAsString());
            }
            catch(ParseException e) {
                CrashHandler.getInstance().handleException("Couldn't parse date from string: " + tag.getAsString(), e, Level.WARN);
                return null;
            }
        });
        boolean optional = tag.getBoolean("optional");

        return new TaskProgress(counter, timer, questStatus, obtained, optional);
    }


    public void toNetwork(FriendlyByteBuf buf) {
        this.counter.toNetwork(buf);
        this.timer.toNetwork(buf);
        buf.writeEnum(this.questStatus);
        buf.writeNullable(this.obtained, FriendlyByteBuf::writeDate);
        buf.writeBoolean(this.optional);
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.put("counter", this.counter.toNBT());
        tag.put("timer", this.timer.toNBT());
        tag.putString("status", this.questStatus.name());
        SerializationHelper.addNullable(this.obtained, tag, "obtained", date -> StringTag.valueOf(DATE_FORMAT.format(date)));
        tag.putBoolean("optional", this.optional);

        return tag;
    }


    public boolean isDone() {
        return this.obtained != null;
    }


    public void revoke() {
        this.obtained = null;
        this.counter.reset();
        this.timer.reset();
    }


    @Nullable
    public Date getObtained() {
        return this.obtained;
    }


    public Component getStatus() {
        MutableComponent component = (MutableComponent) this.counter.getStatus();
        component.append("\n").append(this.timer.getStatus());

        return component;
    }


    public QuestStatus getQuestStatus() {
        return questStatus;
    }


    public void fail() {
        this.questStatus = QuestStatus.FAILED;
        this.grant();
    }


    public void grant() {
        this.obtained = new Date();
    }


    public boolean updateAndCheckTimer() {
        return this.timer.updateAndCheck();
    }


    public boolean updateAndCheckCounter() {
        return this.counter.updateAndCheck();
    }


    public boolean checkTimer() {
        return this.timer.check();
    }


    public boolean checkCounter() {
        return this.counter.check();
    }


    public boolean startTimerIfNeeded() {
        return this.timer.startIfNeeded();
    }


    public void stopTimerIfNeeded() {
        this.timer.stopIfNeeded();
    }


    public MinMax.Timer getTimer() {
        return this.timer;
    }


    public boolean isOptional() {
        return this.optional;
    }


    public enum UpdateMode {
        NONE,
        UPDATE,
        COMPLETE
    }
}