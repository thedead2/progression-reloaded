package de.thedead2.progression_reloaded.api.gui.animation;

@FunctionalInterface
public interface IAnimationType {

    float transform(float from, float to, float duration, float timeLeft, IInterpolationType interpolation);
}
