package de.thedead2.progression_reloaded.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import net.minecraft.Util;
import net.minecraft.client.Timer;


public class TickTimer {

    private final Timer timer = new Timer(20, Util.getMillis());

    private float startTime;

    private float duration;

    private float timeLeft;

    private float startCounter;

    private float sleepTime;

    private boolean inverted;

    private boolean paused;

    private ILoopType loop;


    public TickTimer(float startTime, float duration, ILoopType loop) {
        this(startTime, duration, false, false, loop);
    }


    public TickTimer(float startTime, float duration, boolean inverted, boolean paused, ILoopType loop) {
        this.startTime = startTime;
        this.duration = duration;
        this.inverted = inverted;
        this.paused = paused;
        this.loop = loop;
        this.reset();
    }


    public TickTimer(float startTime, float duration, float timeLeft, float startCounter, float sleepTime, boolean inverted, boolean paused, ILoopType loop) {
        this.startTime = startTime;
        this.duration = duration;
        this.timeLeft = timeLeft;
        this.startCounter = startCounter;
        this.sleepTime = sleepTime;
        this.inverted = inverted;
        this.paused = paused;
        this.loop = loop;
    }


    public void updateTime() {
        int j = this.timer.advanceTime(Util.getMillis());

        if(this.paused || this.sleepTime > 0) {
            this.sleepTime -= j;
            return;
        }

        if(!this.isStarted()) {
            this.startCounter += j;
        }
        else if(this.isFinished()) {
            this.loop.loop(this);
        }
        else {
            if(!this.inverted) {
                this.timeLeft -= j;
            }
            else {
                this.timeLeft += j;
            }
        }
    }


    public TickTimer startIfNeeded() {
        if(!this.isStarted()) {
            this.start();
        }
        return this;
    }


    public boolean isStarted() {
        return this.startCounter > this.startTime;
    }


    public boolean isFinished() {
        return this.inverted ? timeLeft >= this.duration : timeLeft <= 0;
    }


    public TickTimer start() {
        this.pause(false);
        this.reset();
        this.startCounter = this.startTime + 1;
        return this;
    }


    public TickTimer pause(boolean pause) {
        if(this.paused != pause) {
            this.timer.advanceTime(Util.getMillis());
        }
        this.paused = pause;
        return this;
    }


    public TickTimer invert(boolean invert) {
        this.inverted = invert;
        return this;
    }


    public TickTimer loop(ILoopType loop) {
        this.loop = loop;
        return this;
    }


    public boolean isPaused() {
        return this.paused;
    }


    public boolean isLooping() {
        return this.loop.loop(this, false);
    }


    public boolean isInverted() {
        return inverted;
    }


    public float getDuration() {
        return duration;
    }


    public TickTimer setDuration(float duration) {
        this.duration = duration;
        return this;
    }


    public float getStartTime() {
        return startTime;
    }


    public TickTimer setStartTime(float startTime) {
        this.startTime = startTime;
        return this;
    }


    /**
     * Resets the timer to start again.
     **/
    public void reset() {
        this.timer.advanceTime(Util.getMillis());
        this.startCounter = 0;
        this.sleepTime = -1;
        this.timeLeft = this.inverted ? 0 : this.duration;
    }


    public TickTimer stop() {
        this.timeLeft = this.inverted ? this.duration + 1 : -1;
        this.pause(true);
        return this;
    }


    public TickTimer sleep(float time) {
        if(this.sleepTime < 0) {
            this.sleepTime = time;
        }
        return this;
    }

    public float getTimeLeft() {
        return timeLeft;
    }


    public float getTimePassed() {
        return this.duration - this.timeLeft;
    }


    public ILoopType getLoopType() {
        return this.loop;
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("startTime", this.startTime);
        jsonObject.addProperty("duration", this.duration);
        jsonObject.addProperty("timeLeft", this.timeLeft);
        jsonObject.addProperty("startCounter", this.startCounter);
        jsonObject.addProperty("sleepTime", this.sleepTime);
        jsonObject.addProperty("inverted", this.inverted);
        jsonObject.addProperty("paused", this.paused);
        jsonObject.addProperty("loop", this.loop.loop(this, false));

        return null;
    }
}
