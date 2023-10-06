package de.thedead2.progression_reloaded.client.gui.animation;

import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;


public class AnimationTypes {

    public static final IAnimationType LINEAR = (from, to, duration, timeLeft, interpolation) -> {
        float amount = (to - from) / duration;
        float timePassed = duration - timeLeft;
        return from + amount * timePassed;
    };

    public static final IAnimationType EASE_IN = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float timePassed = duration - timeLeft;
        float amount = timePassed / duration;

        return from + dif * interpolation.apply(amount);
    };

    public static final IAnimationType EASE_OUT = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float amount = timeLeft / duration;

        return to - dif * interpolation.apply(amount);
    };

    public static final IAnimationType EASE_IN_OUT = (from, to, duration, timeLeft, interpolation) -> {
        float dif = to - from;
        float halfWay = from + dif / 2;
        float halfTime = duration / 2;
        if(timeLeft > halfTime) {
            return EASE_IN.transform(from, halfWay, halfTime, timeLeft - halfTime, interpolation);
        }
        else {
            return EASE_OUT.transform(halfWay, to, halfTime, timeLeft, interpolation);
        }
    };


    public static IAnimationType STEPS(float steps) {
        return (from, to, duration, timeLeft, interpolation) -> {
            float dif = to - from;
            float amountPerStep = dif / steps;
            float timePerStep = duration / steps;
            int stepsLeft = (int) (timeLeft / timePerStep);
            int stepsPassed = (int) (steps - stepsLeft);

            return from + amountPerStep * stepsPassed;
        };
    }
}
