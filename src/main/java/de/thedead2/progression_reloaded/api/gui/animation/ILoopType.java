package de.thedead2.progression_reloaded.api.gui.animation;

import de.thedead2.progression_reloaded.client.gui.animation.AnimationTimer;


@FunctionalInterface
public interface ILoopType {

    default boolean loop(AnimationTimer timer) {
        return this.loop(timer, true);
    }

    /**
     * Specifies an action to execute when the given {@link AnimationTimer} finishes.
     * Implementations must ensure to only execute the specified action if:
     * <b>{@code shouldRun = true}
     *
     * @param timer     the {@link AnimationTimer} which should be looped or not
     * @param shouldRun whether the specified action, e.g. reset of the timer, should be executed or not,
     *                  use false if you only want to access the return value
     *
     * @return true if the {@link AnimationTimer} is currently looping, false otherwise
     **/
    boolean loop(AnimationTimer timer, boolean shouldRun);
}
