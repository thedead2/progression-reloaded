package de.thedead2.progression_reloaded.client.gui.util.objects;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.progress.IProgressInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.awt.*;


public class ProgressBar extends RenderObject {

    private final ImageRenderObject filled;

    private final ImageRenderObject empty;

    private final IProgressInfo progress;

    private final boolean showPercent;

    private final Font font;


    protected ProgressBar(
            float xPos, float yPos, float zPos, Area.AnchorPoint anchorPoint, float width, float height, Quaternionf xRot, Quaternionf yRot, Quaternionf zRot, Padding padding, ImageRenderObject filled, ImageRenderObject empty, IProgressInfo progress,
            boolean showPercent,
            Font font
    ) {
        super(xPos, yPos, zPos, anchorPoint, width, height, xRot, yRot, zRot, padding);
        this.filled = filled;
        this.empty = empty;
        this.progress = progress;
        this.showPercent = showPercent;
        this.font = font;
    }


    @Override
    public void renderInternal(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.empty.render(poseStack, mouseX, mouseY, partialTick);
        this.filled.setWidth(this.empty.getWidth() * this.progress.getPercent());
        this.filled.render(poseStack, mouseX, mouseY, partialTick);
        if(showPercent) {
            drawString(poseStack, this.font, this.progress.getPercent() * 100 + " %", Math.round(this.empty.getXMax() + 5), Math.round(this.empty.getCenter().y), Color.WHITE.getRGB());
        }
    }
}
