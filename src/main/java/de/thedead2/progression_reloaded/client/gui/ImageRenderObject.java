package de.thedead2.progression_reloaded.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ImageRenderObject extends RenderObject {
    private final ResourceLocation texture;
    private boolean keepRatio;
    private float aspectRatio;
    private Alignment imageAlignment;
    private FixedParameter fixedParameter;
    private final Area.Position originalPosition;
    private final boolean shouldFillScreen;
    private final boolean isInCenter;

    public ImageRenderObject(int renderLayer, int width, int height, Area.Position position, Padding padding, ResourceLocation texture) {
        this(renderLayer, width, height, position, padding, texture, false, 1, FixedParameter.NONE, Alignment.DEFAULT);
    }
    public ImageRenderObject(int renderLayer, int width, int height, Area.Position position, Padding padding, ResourceLocation texture, boolean keepRatio, float aspectRatio, FixedParameter fixedParameter, Alignment imageAlignment) {
        super(renderLayer, width, height, position, padding);
        this.texture = texture;
        this.keepRatio = keepRatio;
        this.aspectRatio = aspectRatio;
        this.imageAlignment = imageAlignment;
        this.fixedParameter = fixedParameter;

        this.originalPosition = position;
        this.shouldFillScreen = RenderUtil.getScreenWidth() == width && RenderUtil.getScreenHeight() == height;
        this.isInCenter = RenderUtil.getScreenCenter(width, height).equals(this.getCenter());
    }

    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        int width = this.getWidth();
        int height = this.getHeight();
        int relativeWidth = (keepRatio && fixedParameter == FixedParameter.HEIGHT && height != 0 ? Math.round(this.getRelativeWidth(height)) : width);
        int relativeHeight = (keepRatio && fixedParameter == FixedParameter.WIDTH && width != 0 ? Math.round(this.getRelativeHeight(width)) : height);
        int uOffset = keepRatio ? Math.negateExact(imageAlignment.getXPos(width, relativeWidth)) : 0;
        int vOffset = keepRatio ? Math.negateExact(imageAlignment.getYPos(height, relativeHeight)) : 0;

        RenderSystem.setShaderTexture(renderLayer, texture);
        blit(poseStack, this.renderArea.getXMin(), this.renderArea.getYMin(), uOffset, vOffset, this.renderArea.getWidth(), this.renderArea.getHeight(), relativeWidth, relativeHeight);
    }

    /** Returns the relative width of the object for the given height **/
    private float getRelativeWidth(int height){
        return height*aspectRatio;
    }
    /** Returns the relative height of the object for the given width **/
    private float getRelativeHeight(int width){
        return width/aspectRatio;
    }

    public void enableRatioKeeping(boolean keepRatio, float aspectRatio, FixedParameter fixedParameter, Alignment imageAlignment){
        this.keepRatio = keepRatio;
        this.aspectRatio = aspectRatio;
        this.fixedParameter = fixedParameter;
        this.imageAlignment = imageAlignment;
    }

    public void disableRatioKeeping(){
        this.keepRatio = false;
        this.aspectRatio = 1;
        this.fixedParameter = FixedParameter.NONE;
        this.imageAlignment = Alignment.DEFAULT;
    }

    public void onResize(int screenWidth, int screenHeight) {
        if(shouldFillScreen){
            this.setWidth(screenWidth);
            this.setHeight(screenHeight);
        }
        if(keepRatio && shouldFillScreen){
            int width = this.getWidth();
            int height = this.getHeight();
            int relativeWidth = (fixedParameter == FixedParameter.HEIGHT && height != 0 ? Math.round(this.getRelativeWidth(height)) : width);
            int relativeHeight = (fixedParameter == FixedParameter.WIDTH && width != 0 ? Math.round(this.getRelativeHeight(width)) : height);

            if(screenWidth > relativeWidth){
                this.setWidth(relativeWidth);
            }
            else if(screenHeight > relativeHeight){
                this.setHeight(relativeHeight);
            }
            else if (!this.position.equals(originalPosition)){
                this.setPosition(originalPosition);
            }
        }
        if(isInCenter) this.setPosition(RenderUtil.getScreenCenter(screenWidth, screenHeight), Area.Point.CENTER);

    }

    enum FixedParameter {
        WIDTH,
        HEIGHT,
        NONE;
    }

    public static class Builder extends RenderObject.Builder<ImageRenderObject> {

        @Override
        public ImageRenderObject build() {
            return null;
        }
    }
}
