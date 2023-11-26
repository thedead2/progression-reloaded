package de.thedead2.progression_reloaded.client.gui.animation;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import it.unimi.dsi.fastutil.floats.FloatConsumer;


public class EmptyAnimation implements IAnimation {

    private final AnimationTimer timer;


    public EmptyAnimation(float duration, boolean started) {
        this.timer = new AnimationTimer(0, duration, false, !started, LoopTypes.NO_LOOP);
    }


    @Override
    public IAnimation invert(boolean inverted) {
        this.timer.updateTime();
        return this;
    }


    @Override
    public IAnimation start() {
        this.timer.updateTime();
        this.timer.start();
        return this;
    }


    @Override
    public IAnimation pause(boolean paused) {
        this.timer.updateTime();
        this.timer.pause(paused);
        return this;
    }


    @Override
    public IAnimation stop() {
        this.timer.updateTime();
        this.timer.stop();
        return this;
    }


    @Override
    public IAnimation loop(ILoopType loop) {
        this.timer.updateTime();
        return this;
    }


    @Override
    public IAnimation animate(float from, float to, FloatConsumer consumer) {
        this.timer.updateTime();
        return this;
    }


    @Override
    public IAnimation animateWithKeyframes(float from, float to, FloatConsumer consumer, Keyframe... keyframes) {
        this.timer.updateTime();
        return this;
    }


    @Override
    public boolean isStarted() {
        this.timer.updateTime();
        return this.timer.isStarted();
    }


    @Override
    public boolean isFinished() {
        this.timer.updateTime();
        return this.timer.isFinished();
    }


    @Override
    public boolean isPaused() {
        this.timer.updateTime();
        return this.timer.isPaused();
    }


    @Override
    public boolean isLooping() {
        this.timer.updateTime();
        return false;
    }


    @Override
    public boolean isInverted() {
        this.timer.updateTime();
        return false;
    }


    @Override
    public IAnimation reset() {
        this.timer.updateTime();
        this.timer.reset();
        return this;
    }


    @Override
    public JsonElement toJson() {
        return this.timer.toJson();
    }


    @Override
    public IAnimation sleep(float ticks) {
        this.timer.updateTime();
        this.timer.sleep(ticks);
        return this;
    }
}
