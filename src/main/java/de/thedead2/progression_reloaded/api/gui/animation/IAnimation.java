package de.thedead2.progression_reloaded.api.gui.animation;

import de.thedead2.progression_reloaded.client.gui.animation.AnimationTimer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;


public interface IAnimation {

    IAnimation invert(boolean inverted);

    IAnimation pause(boolean paused);

    IAnimation loop(ILoopType loop);

    /**
     * Animates a value in the given interval.
     *
     * @param from     the start point of the value to animate
     * @param to       the end point of the value to animate
     * @param consumer the action to which the animated value should be applied to
     **/
    void animate(float from, float to, FloatConsumer consumer);

    /**
     * @return true if this {@link IAnimation} has started
     **/
    boolean isStarted();

    /**
     * @return true if this animation has finished
     * <p>Note: this also returns true for a brief moment while this {@link IAnimation} is looping</p>
     *
     * @see #isLooping()
     **/
    boolean isFinished();

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
     **/
    void reset();
}
