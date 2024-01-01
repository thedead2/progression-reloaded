package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableItemTexture;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.client.gui.util.TooltipInfo;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class QuestWidget extends ScreenComponent {

    private final IAnimation rotation = new SimpleAnimation(0, MathHelper.secondsToTicks(20), LoopTypes.LOOP, AnimationTypes.EASE_IN, InterpolationTypes.LINEAR);

    private final IAnimation scaling = new SimpleAnimation(0, MathHelper.secondsToTicks(1f), LoopTypes.LOOP_INVERSE_WHILE(animationTimer -> this.isMouseOver()), AnimationTypes.EASE_IN_OUT, InterpolationTypes.SINUS, false);

    private final DrawableTexture frame;

    private final DrawableItemTexture item;

    private final Tooltip tooltip;


    public QuestWidget(Area area, TextureInfo frame, QuestDisplayInfo quest, TooltipInfo tooltipInfo) {
        super(area);
        this.frame = new DrawableTexture(frame, this.area);
        this.item = new DrawableItemTexture(this.area, quest.icon());
        this.tooltip = new Tooltip(tooltipInfo, quest.description(), 100, 300);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.pushPose();
        this.item.render(poseStack, mouseX, mouseY, partialTick);
        this.animateHoverEffect(poseStack, mouseX, mouseY);
        this.frame.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
        if(this.isMouseOver(mouseX, mouseY)) {
            this.tooltip.render(poseStack, mouseX, mouseY, partialTick);
        }
    }


    private void animateHoverEffect(PoseStack poseStack, double mouseX, double mouseY) {
        Vector3f anchor = new Vector3f(this.frame.getArea().getCenterX(), this.frame.getArea().getCenterY(), this.frame.getArea().getZ());
        this.rotation.animateIf(iAnimation -> this.frame.isMouseOver(mouseX, mouseY), 0, 359.99999999999f, t -> RenderUtil.rotateAround(poseStack, Axis.ZP.rotationDegrees(t), anchor));

        if(this.frame.isMouseOver(mouseX, mouseY)) {
            this.scaling.startIfNeeded();
        }
        else {
            this.scaling.invert(this.scaling.isStarted());
        }

        this.scaling.animate(1, 1.25f, t -> RenderUtil.scaleAround(poseStack, t, t, 1, anchor.x(), anchor.y(), anchor.z()));
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
