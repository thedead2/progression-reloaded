package de.thedead2.progression_reloaded.client.gui.animation;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


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
    public SimpleAnimation animate(float from, float to, FloatConsumer consumer) {
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
        return this;
    }


    @Override
    public SimpleAnimation animateWithKeyframes(float from, float to, FloatConsumer consumer, Keyframe... keyframes) {
        IAnimation.animateWithKeyframes(this.timer, this.animationType, this.interpolationType, from, to, consumer, keyframes);
        return this;
    }


    @Override
    public boolean isStarted() {
        return this.timer.isStarted();
    }


    @Override
    public boolean isFinished() {
        return this.timer.isFinished();
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
    public SimpleAnimation reset() {
        this.timer.reset();
        return this;
    }
}
