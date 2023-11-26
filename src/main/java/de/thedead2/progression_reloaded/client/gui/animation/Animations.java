package de.thedead2.progression_reloaded.client.gui.animation;

import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.ILoopType;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;


public class Animations {

    public static boolean bool = false;


    public static RenderableAnimation SPIN_AROUND(IAnimation iAnimation, Vector3f anchor, Axis axis, ScreenComponent component) {
        return new RenderableAnimation(iAnimation, (animation, poseStack, mouseX, mouseY, partialTick) -> {
            poseStack.pushPose();
            animation.animate(0, 359.999999999999f, t -> RenderUtil.rotateAround(poseStack, axis.rotationDegrees(t), anchor));
            component.render(poseStack, mouseX, mouseY, partialTick);
            poseStack.popPose();
        });
    }


    public static RenderableAnimation TYPE_WRITER_ANIMATION(float duration, ILoopType loopType, TextBox textBox, FormattedString text, boolean withCursor) {
        return TYPE_WRITER_ANIMATION(duration, loopType, textBox, new FormattedCharSeq(text), withCursor);
    }


    public static RenderableAnimation TYPE_WRITER_ANIMATION(float duration, ILoopType loopType, TextBox textBox, FormattedCharSeq text, boolean withCursor) {
        SimpleAnimation simpleAnimation = new SimpleAnimation(0, duration, loopType, AnimationTypes.STEPS(text.length()), InterpolationTypes.LINEAR, false);
        textBox.setRenderCursor(() -> textBox.isEditable() || (withCursor && !simpleAnimation.isFinished()));

        final int[] previousVal = new int[1];
        previousVal[0] = simpleAnimation.isInverted() ? text.length() - 1 : 0;
        return new RenderableAnimation(simpleAnimation, (animation, poseStack, mouseX, mouseY, partialTick) -> {
            bool = true;
            animation.animate(0, text.length(), t -> {
                if(animation.isFinishedButLooping()) {
                    if(!animation.isInverted()) {
                        textBox.clear();
                        previousVal[0] = 0;
                    }
                    else {
                        previousVal[0] = text.length() - 1;//FIXME
                    }
                }
                else {
                    int roundedT = Mth.clamp(Math.round(t), 0, text.length());
                    if(!animation.isInverted()) {
                        textBox.insertText(text.subSeq(previousVal[0], roundedT));
                    }
                    else {
                        textBox.deleteText(previousVal[0] - roundedT);
                    }
                    previousVal[0] = roundedT;
                }
            });
            bool = false;

            textBox.render(poseStack, mouseX, mouseY, partialTick);
        });
    }
}
