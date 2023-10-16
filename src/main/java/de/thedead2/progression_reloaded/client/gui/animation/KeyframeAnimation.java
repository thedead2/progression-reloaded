package de.thedead2.progression_reloaded.client.gui.animation;

import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.util.Mth;


public class KeyframeAnimation implements IAnimation {

    private final IAnimationType animationType;

    private final IInterpolationType interpolationType;

    private final Keyframe[] keyframes;

    private final AnimationTimer timer;


    public KeyframeAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, Keyframe... keyframes) {
        this.animationType = animationType;
        this.interpolationType = interpolationType;
        this.keyframes = keyframes;
        this.timer = new AnimationTimer(startTime, duration, loop);
    }


    @Override
    public KeyframeAnimation invert(boolean inverted) {
        this.timer.invert(inverted);
        return this;
    }


    @Override
    public KeyframeAnimation pause(boolean paused) {
        this.timer.pause(paused);
        return this;
    }


    @Override
    public KeyframeAnimation loop(ILoopType loop) {
        this.timer.loop(loop);
        return this;
    }


    @Override
    public KeyframeAnimation animate(float from, float to, FloatConsumer consumer) {
        return this.animateWithKeyframes(from, to, consumer, this.keyframes);
    }


    public KeyframeAnimation animateWithKeyframes(float from, float to, FloatConsumer consumer, Keyframe... keyframes) {
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
    public KeyframeAnimation reset() {
        this.timer.reset();
        return this;
    }
}
