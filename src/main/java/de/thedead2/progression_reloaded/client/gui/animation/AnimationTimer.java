package de.thedead2.progression_reloaded.client.gui.animation;

import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import net.minecraft.Util;
import net.minecraft.client.Timer;


public class AnimationTimer {

    private final Timer timer = new Timer(20, Util.getMillis());

    private float startTime;

    private float duration;

    private float timeLeft;

    private float sleepCounter;

    private boolean inverted;

    private boolean paused;

    private ILoopType loop;


    public AnimationTimer(float startTime, float duration, ILoopType loop) {
        this(startTime, duration, false, false, loop);
    }


    public AnimationTimer(float startTime, float duration, boolean inverted, boolean paused, ILoopType loop) {
        this.startTime = startTime;
        this.duration = duration;
        this.inverted = inverted;
        this.paused = paused;
        this.loop = loop;
        this.reset();
    }


    /**
     * Resets the timer to start again.
     **/
    public void reset() {
        this.timer.advanceTime(Util.getMillis());
        this.sleepCounter = 0;
        this.timeLeft = this.inverted ? 0 : this.duration;
    }


    public AnimationTimer invert(boolean invert) {
        this.inverted = invert;
        return this;
    }


    public AnimationTimer pause(boolean pause) {
        this.paused = pause;
        return this;
    }


    public AnimationTimer loop(ILoopType loop) {
        this.loop = loop;
        return this;
    }


    public void updateTime() {
        int j = this.timer.advanceTime(Util.getMillis());

        if(!this.isStarted()) {
            this.sleepCounter += j;
        }
        else if(this.isFinished()) {
            this.loop.loop(this);
        }
        else {
            if(this.paused) {
                return;
            }
            if(!this.inverted) {
                this.timeLeft -= j;
            }
            else {
                this.timeLeft += j;
            }
        }
    }


    public boolean isStarted() {
        return this.sleepCounter > this.startTime;
    }


    public boolean isFinished() {
        return this.inverted ? timeLeft > this.duration : timeLeft < 0;
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


    public AnimationTimer setDuration(float duration) {
        this.duration = duration;
        return this;
    }


    public float getStartTime() {
        return startTime;
    }


    public AnimationTimer setStartTime(float startTime) {
        this.startTime = startTime;
        return this;
    }


    public float getTimeLeft() {
        return timeLeft;
    }


    public float getTimePassed() {
        return this.duration - this.timeLeft;
    }
}
