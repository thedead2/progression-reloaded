package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DecimalFormat;


public class ProgressBar extends ScreenComponent {

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00 %");

    private final SimpleAnimation animation;

    private final DrawableTexture empty;

    private final DrawableTexture filled;

    private float percent;
    private final boolean showPercent;
    private final Font font;

    private float previousPercent;


    public ProgressBar(Area area, TextureInfo empty, TextureInfo filled, IProgressInfo progress, boolean showPercent, Font font) {
        super(area);
        this.animation = new SimpleAnimation(0, MathHelper.secondsToTicks(2), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC);
        this.empty = new DrawableTexture(empty, this.area);
        this.filled = new DrawableTexture(filled, this.area.copy());
        this.percent = progress.getPercent();
        this.showPercent = showPercent;
        this.font = font;
    }


    public void updateProgress(IProgressInfo progress) {
        this.previousPercent = this.percent;
        this.percent = progress.getPercent();
        animation.reset();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        this.animation.animate(this.previousPercent, this.percent, percent -> { //TODO: When level complete don't animate from current percent to 0 --> instead animate from current percent to 100 % and then jump to 0 %
            this.drawBar(poseStack, percent);
            if(showPercent) {
                String string = PERCENT_FORMAT.format(percent);
                int stringWidth = this.font.width(string);
                int stringHeight = this.font.lineHeight;
                this.font.drawShadow(poseStack, string, this.area.getCenterX() - (float) stringWidth / 2, this.area.getCenterY() - (float) stringHeight / 2, Color.WHITE.getRGB());
            }
        });
        poseStack.popPose();
    }


    private void drawBar(PoseStack poseStack, float percentFilled) {
        this.empty.draw(poseStack);
        this.filled.setRenderWidth(this.empty.getRenderWidth() * percentFilled);
        this.filled.draw(poseStack);
    }
}
