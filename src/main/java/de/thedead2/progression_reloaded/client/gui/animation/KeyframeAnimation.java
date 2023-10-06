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
            int currentIndex = Mth.binarySearch(0, keyframes.length, (i) -> this.timer.getTimePassed() <= keyframes[i].timeStamp()) - 1;
            int nextIndex = currentIndex + 1;
            Keyframe currentKeyframe = currentIndex >= 0 ? keyframes[currentIndex] : new Keyframe(from, this.timer.getStartTime(), this.animationType, this.interpolationType);
            Keyframe nextKeyframe = nextIndex <= keyframes.length - 1 ? keyframes[nextIndex] : new Keyframe(to, this.timer.getDuration(), this.animationType, this.interpolationType);
            float timeSinceTimeStamp = this.timer.getTimePassed() - currentKeyframe.timeStamp();
            float timeBetweenKeyframes = nextKeyframe.timeStamp() - currentKeyframe.timeStamp();
            float timeLeftBetweenKeyframes = timeBetweenKeyframes - timeSinceTimeStamp;
            f = currentKeyframe.animationType().transform(currentKeyframe.setPoint(), nextKeyframe.setPoint(), timeBetweenKeyframes, timeLeftBetweenKeyframes, currentKeyframe.interpolationType());
        }
        consumer.accept(f);
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


    /**
     * Resets the animation to start again.
     **/
    @Override
    public void reset() {
        this.timer.reset();
    }
}
