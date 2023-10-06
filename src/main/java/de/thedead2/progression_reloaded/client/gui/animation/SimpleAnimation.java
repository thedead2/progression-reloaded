package de.thedead2.progression_reloaded.client.gui.animation;

import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import it.unimi.dsi.fastutil.floats.FloatConsumer;


public class SimpleAnimation implements IAnimation {

    private final IAnimationType animationType;

    private final IInterpolationType interpolationType;

    private final AnimationTimer timer;


    public SimpleAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType) {
        this.animationType = animationType;
        this.interpolationType = interpolationType;
        this.timer = new AnimationTimer(startTime, duration, loop);
    }


    @Override
    public SimpleAnimation invert(boolean inverted) {
        this.timer.invert(inverted);
        return this;
    }


    @Override
    public SimpleAnimation pause(boolean paused) {
        this.timer.pause(paused);
        return this;
    }


    @Override
    public SimpleAnimation loop(ILoopType loop) {
        this.timer.loop(loop);
        return this;
    }


    @Override
    public void animate(float from, float to, FloatConsumer consumer) {
        this.timer.updateTime();
        float f;
        if(!this.isStarted()) {
            f = this.timer.isInverted() ? to : from;
        }
        else if(this.isFinished()) {
            f = this.timer.isInverted() ? from : to;
        }
        else {
            f = this.animationType.transform(from, to, this.timer.getDuration(), this.timer.getTimeLeft(), this.interpolationType);
        }
        consumer.accept(f);
    }


    @Override
    public boolean isStarted() {
        return this.timer.isStarted();
    }


    @Override
    public boolean isFinished() {
        return this.timer.isFinished(); //TODO: Check for loops!
    }


    @Override
    public boolean isPaused() {
        return this.timer.isPaused();
    }


    @Override
    public boolean isLooping() {
        return this.timer.isLooping();
    }


    @Override
    public void reset() {
        this.timer.reset();
    }
}
