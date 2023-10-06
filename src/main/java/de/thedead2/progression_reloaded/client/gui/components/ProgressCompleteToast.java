package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public class ProgressCompleteToast extends ScreenComponent {

    private final IDisplayInfo displayInfo;

    private final DrawableTexture toastTexture;

    private final IAnimation animation = new SimpleAnimation(0, MathHelper.secondsToTicks(5), LoopTypes.LOOP_TIMES_INVERSE(2), AnimationTypes.EASE_IN_OUT, InterpolationTypes.EXPONENTIAL);/*new KeyframeAnimation(0, MathHelper.secondsToTicks(10),
    LoopTypes
    .NO_LOOP, AnimationTypes
    .EASE_IN_OUT,
    InterpolationTypes.SINUS,
                                                               new Keyframe(1f, MathHelper.secondsToTicks(3), AnimationTypes.LINEAR, InterpolationTypes.LINEAR),
                                                               new Keyframe(1f, MathHelper.secondsToTicks(7), AnimationTypes.EASE_IN_OUT, InterpolationTypes.SINUS)
                                                               );*/

    private final Font font;


    public ProgressCompleteToast(Area area, IDisplayInfo displayInfo, TextureInfo toastTexture, Font font) {
        super(area);
        this.displayInfo = displayInfo;
        this.toastTexture = new DrawableTexture(toastTexture, this.area);
        this.font = font;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        this.animation.animate(0, 1, t -> {
            this.toastTexture.setAlpha(t);
            this.toastTexture.draw(poseStack);
            //RenderSystem.setShaderColor(1, 1, 1, t);
            RenderUtil.renderItem(poseStack, this.area.getInnerX(), this.area.getInnerY(), 1, this.displayInfo.getIcon());
            this.font.draw(poseStack, this.displayInfo.getTitle(), this.area.getInnerX() + 16 + 5, this.area.getInnerY(), Color.WHITE.getRGB() | Mth.ceil(t * 255.0F) << 24);

        });
        poseStack.popPose();
    }


    public boolean shouldRender() {
        return !this.animation.isFinished() || this.animation.isLooping();
    }
}
