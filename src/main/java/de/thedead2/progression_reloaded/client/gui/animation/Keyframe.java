package de.thedead2.progression_reloaded.client.gui.animation;


import de.thedead2.progression_reloaded.api.gui.animation.IAnimationType;
import de.thedead2.progression_reloaded.api.gui.animation.IInterpolationType;


public record Keyframe(float setPoint, float timeStamp, IAnimationType animationType, IInterpolationType interpolationType) {
}
