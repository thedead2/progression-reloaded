package de.thedead2.progression_reloaded.client.gui.animation;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import net.minecraft.util.Mth;


public class KeyframeAnimation extends AbstractAnimation {
    private final Keyframe[] keyframes;


    public KeyframeAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, Keyframe... keyframes) {
        this(startTime, duration, loop, animationType, interpolationType, true, keyframes);
    }


    public KeyframeAnimation(float startTime, float duration, ILoopType loop, IAnimationType animationType, IInterpolationType interpolationType, boolean started, Keyframe... keyframes) {
        super(startTime, duration, loop, animationType, interpolationType, started);
        this.keyframes = keyframes;
    }


    @Override
    public JsonElement toJson() {
        return null;
    }


    @Override
    protected float animateInternal(float from, float to) {
        int currentIndex = Mth.binarySearch(0, this.keyframes.length, (i) -> this.timer.getTimePassed() <= this.keyframes[i].timeStamp()) - 1;
        int nextIndex = currentIndex + 1;
        Keyframe currentKeyframe = currentIndex >= 0 ? this.keyframes[currentIndex] : new Keyframe(this.timer.getStartTime(), this.animationType, this.interpolationType, from);
        Keyframe nextKeyframe = nextIndex <= this.keyframes.length - 1 ? this.keyframes[nextIndex] : new Keyframe(this.timer.getDuration(), this.animationType, this.interpolationType, to);
        float timeSinceTimeStamp = this.timer.getTimePassed() - currentKeyframe.timeStamp();
        float timeBetweenKeyframes = nextKeyframe.timeStamp() - currentKeyframe.timeStamp();
        float timeLeftBetweenKeyframes = timeBetweenKeyframes - timeSinceTimeStamp;

        return currentKeyframe.animationType().transform(currentKeyframe.setPoint(), nextKeyframe.setPoint(), timeBetweenKeyframes, timeLeftBetweenKeyframes, currentKeyframe.interpolationType());
    }
}
