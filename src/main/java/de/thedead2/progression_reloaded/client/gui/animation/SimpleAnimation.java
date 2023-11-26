package de.thedead2.progression_reloaded.client.gui.animation;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;


public class SimpleAnimation extends AbstractAnimation {

    public SimpleAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType) {
        super(startTime, duration, loop, animationType, interpolationType);
    }


    public SimpleAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, boolean started) {
        super(startTime, duration, loop, animationType, interpolationType, started);
    }


    @Override
    public JsonElement toJson() {
        return null;
    }


    @Override
    protected float animateInternal(float from, float to) {
        return this.animationType.transform(from, to, this.timer.getDuration(), this.timer.getTimeLeft(), this.interpolationType);
    }
}
