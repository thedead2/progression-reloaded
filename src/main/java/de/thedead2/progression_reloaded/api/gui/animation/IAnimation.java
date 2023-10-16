package de.thedead2.progression_reloaded.api.gui.animation;

import de.thedead2.progression_reloaded.client.gui.animation.AnimationTimer;
import de.thedead2.progression_reloaded.client.gui.animation.Keyframe;
import de.thedead2.progression_reloaded.client.gui.animation.KeyframeAnimation;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.util.Mth;


public interface IAnimation {

    /**
     * Inverts the play direction of this animation.
     * @param inverted true if the animation should run backwards, false otherwise
     * **/
    IAnimation invert(boolean inverted);

    /**
     * Pauses or resumes this animation
     * @param paused true if this animation should be paused, false if it should be resumed or not paused
     * **/
    IAnimation pause(boolean paused);

    /**
     * Sets the {@link ILoopType} for this animation.
     * @see ILoopType
     * **/
    IAnimation loop(ILoopType loop);

    /**
     * Animates a value in the given interval.
     *
     * @param from     the start point of the value to animate
     * @param to       the end point of the value to animate
     * @param consumer the action to which the animated value should be applied to
     * @return this
     **/
    IAnimation animate(float from, float to, FloatConsumer consumer);

    /**
     * Animates a value in the given interval following the {@link Keyframe#setPoint()}
     * <p>Same as creating a new {@link KeyframeAnimation#KeyframeAnimation(float, float, ILoopType, IAnimationType, IInterpolationType, Keyframe...)} and then calling {@link #animate(float, float, FloatConsumer)} on it.</p>
     * @param from     the start point of the value to animate
     * @param to       the end point of the value to animate
     * @param consumer the action to which the animated value should be applied to
     * @param keyframes the keyframes for the animation
     * @see IAnimation#animateWithKeyframes(AnimationTimer, IAnimationType, IInterpolationType, float, float, FloatConsumer, Keyframe...)
     * **/
    IAnimation animateWithKeyframes(float from, float to, FloatConsumer consumer, Keyframe... keyframes);

    /**
     * @return true if this {@link IAnimation} has started
     **/
    boolean isStarted();

    /**
     * @return true if this animation has finished
     * <p>Note: this also returns true for a brief moment while this {@link IAnimation} is looping</p>
     *
     * @see #isLooping()
     * @see #isFinishedAndNotLooping()
     **/
    boolean isFinished();

    /**
     * @return true if this animation has finished and is not currently looping
     * **/
    default boolean isFinishedAndNotLooping() {
        return this.isFinished() && !this.isLooping();
    }

    /**
     * @return true if this {@link IAnimation} is currently paused
     **/
    boolean isPaused();

    /**
     * @return true if this animation is currently looping
     *
     * @see ILoopType#loop(AnimationTimer, boolean)
     * @see #isFinished()
     **/
    boolean isLooping();

    /**
     * Resets this {@link IAnimation} to start again
     * @return this
     **/
    IAnimation reset();

    /**
     * Adds another animation that should be played after this animation has finished.
     *
     * @param other the other animation that should be played after this one
     * @return the other animation
     * @see #animate(float, float, FloatConsumer)
     **/
    default IAnimation andThenAnimate(IAnimation other, float from, float to, FloatConsumer consumer) {
        other.pause(!this.isFinished() || this.isLooping());
        return other.animate(from, to, consumer);
    }
    /**
     * Adds another animation with the given keyframes that should be played after this animation has finished.
     *
     * @param other the other animation that should be played after this one
     * @return the other animation
     * @see #andThenAnimate(IAnimation, float, float, FloatConsumer)
     **/
    default IAnimation andThenAnimateWithKeyframes(IAnimation other, float from, float to, FloatConsumer consumer, Keyframe... keyframes) {
        other.pause(!this.isFinished() || this.isLooping());
        return other.animateWithKeyframes(from, to, consumer, keyframes);
    }

    static void animateWithKeyframes(AnimationTimer timer, IAnimationType animationType, IInterpolationType interpolationType, float from, float to, FloatConsumer consumer, Keyframe... keyframes) {
        timer.updateTime();
        float f;
        if(!timer.isStarted()) {
            f = timer.isInverted() ? to : from;
        }
        else if(timer.isFinished()) {
            f = timer.isInverted() ? from : to;
        }
        else {
            int currentIndex = Mth.binarySearch(0, keyframes.length, (i) -> timer.getTimePassed() <= keyframes[i].timeStamp()) - 1;
            int nextIndex = currentIndex + 1;
            Keyframe currentKeyframe = currentIndex >= 0 ? keyframes[currentIndex] : new Keyframe(timer.getStartTime(), animationType, interpolationType, from);
            Keyframe nextKeyframe = nextIndex <= keyframes.length - 1 ? keyframes[nextIndex] : new Keyframe(timer.getDuration(), animationType, interpolationType, to);
            float timeSinceTimeStamp = timer.getTimePassed() - currentKeyframe.timeStamp();
            float timeBetweenKeyframes = nextKeyframe.timeStamp() - currentKeyframe.timeStamp();
            float timeLeftBetweenKeyframes = timeBetweenKeyframes - timeSinceTimeStamp;
            f = currentKeyframe.animationType().transform(currentKeyframe.setPoint(), nextKeyframe.setPoint(), timeBetweenKeyframes, timeLeftBetweenKeyframes, currentKeyframe.interpolationType());
        }
        consumer.accept(f);
    }
}
