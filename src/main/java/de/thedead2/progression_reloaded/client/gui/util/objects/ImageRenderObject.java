package de.thedead2.progression_reloaded.client.gui.util.objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;


public class ImageRenderObject extends RenderObject {

    private final ResourceLocation texture;

    private boolean keepRatio;

    private float aspectRatio;

    private Alignment imageAlignment;

    private FixedParameter fixedParameter;


    public ImageRenderObject(float xPos, float yPos, float zPos, Area.AnchorPoint anchorPoint, float width, float height, Padding padding, ResourceLocation texture) {
        super(xPos, yPos, zPos, anchorPoint, width, height, padding);
        this.texture = texture;
    }


    public ImageRenderObject(float xPos, float yPos, float zPos, Area.AnchorPoint anchorPoint, float width, float height, Quaternionf xRot, Quaternionf yRot, Quaternionf zRot, Padding padding, ResourceLocation texture) {
        super(xPos, yPos, zPos, anchorPoint, width, height, xRot, yRot, zRot, padding);
        this.texture = texture;
    }


    @Override
    public void renderInternal(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float width = this.getWidth();
        float height = this.getHeight();
        float relativeWidth = (keepRatio && fixedParameter == FixedParameter.HEIGHT && height != 0 ? Math.round(this.getRelativeWidth(height)) : width);
        float relativeHeight = (keepRatio && fixedParameter == FixedParameter.WIDTH && width != 0 ? Math.round(this.getRelativeHeight(width)) : height);
        int uOffset = keepRatio ? Math.negateExact(Math.round(imageAlignment.getXPos(width, relativeWidth))) : 0;
        int vOffset = keepRatio ? Math.negateExact(Math.round(imageAlignment.getYPos(height, relativeHeight))) : 0;

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        blit(poseStack, Math.round(this.renderArea.getXMin()), Math.round(this.renderArea.getYMin()), uOffset, vOffset, Math.round(this.renderArea.getWidth()), Math.round(this.renderArea.getHeight()), Math.round(relativeWidth), Math.round(relativeHeight));
        RenderSystem.disableBlend();
    }


    /**
     * Returns the relative width of the object for the given height
     **/
    private float getRelativeWidth(float height) {
        return height * aspectRatio;
    }


    /**
     * Returns the relative height of the object for the given width
     **/
    private float getRelativeHeight(float width) {
        return width / aspectRatio;
    }


    public void enableRatioKeeping(float aspectRatio, FixedParameter fixedParameter, Alignment imageAlignment) {
        this.keepRatio = true;
        this.aspectRatio = aspectRatio;
        this.fixedParameter = fixedParameter;
        this.imageAlignment = imageAlignment;
    }


    public void disableRatioKeeping() {
        this.keepRatio = false;
        this.aspectRatio = 1;
        this.fixedParameter = FixedParameter.NONE;
        this.imageAlignment = Alignment.DEFAULT;
    }


    public enum FixedParameter {
        WIDTH,
        HEIGHT,
        NONE
    }

    public static class Builder extends RenderObject.Builder<ImageRenderObject> {

        @Override
        public ImageRenderObject build() {
            return null;
        }
    }
}
