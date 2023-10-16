package de.thedead2.progression_reloaded.client.gui.animation;

import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;


public class LoopTypes {

    public static final ILoopType NO_LOOP = (timer, shouldRun) -> false;

    public static final ILoopType LOOP = (timer, shouldRun) -> {
        if(shouldRun) {
            timer.reset();
        }
        return true;
    };

    public static final ILoopType LOOP_INVERSE = (timer, shouldRun) -> {
        if(shouldRun) {
            timer.invert(!timer.isInverted());
            timer.reset();
        }
        return true;
    };


    public static ILoopType LOOP_TIMES(int amount) {
        AtomicInteger i = new AtomicInteger(0);
        return (timer, shouldRun) -> {
            if(i.get() < amount) {
                if(shouldRun) {
                    i.getAndIncrement();
                    LOOP.loop(timer, true);
                }
                return true;
            }
            return false;
        };
    }


    public static ILoopType LOOP_TIMES_INVERSE(int amount) {
        AtomicInteger i = new AtomicInteger(0);
        return (timer, shouldRun) -> {
            if(i.get() < amount) {
                if(shouldRun) {
                    i.getAndIncrement();
                    LOOP_INVERSE.loop(timer, true);
                }
                return true;
            }
            return false;
        };
    }

    /**
     * Creates a {@link ILoopType} that loops until the given {@link Predicate} returns true.
     * @param predicate the predicate to test
     * **/
    public static ILoopType LOOP_UNTIL(Predicate<AnimationTimer> predicate) {
        return (timer, shouldRun) -> {
            if(!predicate.test(timer)) {
                LOOP.loop(timer, true);
                return true;
            }
            return false;
        };
    }
    /**
     * Same as {@link #LOOP_UNTIL(Predicate)} but in inverse direction
     * **/
    public static ILoopType LOOP_INVERSE_UNTIL(Predicate<AnimationTimer> predicate) {
        return (timer, shouldRun) -> {
            if(!predicate.test(timer)) {
                LOOP_INVERSE.loop(timer, true);
                return true;
            }
            return false;
        };
    }
}
