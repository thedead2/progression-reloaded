package de.thedead2.progression_reloaded.client.gui.util.objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.util.*;
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

    public ImageRenderObject(int renderLayer, int width, int height, Area.Position position, Padding padding, PoseStackTransformer poseStackTransformer, ResourceLocation texture) {
        this(renderLayer, width, height, position, padding, poseStackTransformer, texture, false, 1, FixedParameter.NONE, Alignment.DEFAULT);
    }
    public ImageRenderObject(int renderLayer, int width, int height, Area.Position position, Padding padding, PoseStackTransformer poseStackTransformer, ResourceLocation texture, boolean keepRatio, float aspectRatio, FixedParameter fixedParameter, Alignment imageAlignment) {
        super(renderLayer, width, height, position, padding, poseStackTransformer);
        this.texture = texture;
        this.keepRatio = keepRatio;
        this.aspectRatio = aspectRatio;
        this.imageAlignment = imageAlignment;
        this.fixedParameter = fixedParameter;

        this.originalPosition = position;
        this.shouldFillScreen = RenderUtil.getScreenWidth() == width && RenderUtil.getScreenHeight() == height;
        this.isInCenter = RenderUtil.getScreenCenter().equals(this.getCenter());
    }

    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        int width = this.getWidth();
        int height = this.getHeight();
        int relativeWidth = (keepRatio && fixedParameter == FixedParameter.HEIGHT && height != 0 ? Math.round(this.getRelativeWidth(height)) : width);
        int relativeHeight = (keepRatio && fixedParameter == FixedParameter.WIDTH && width != 0 ? Math.round(this.getRelativeHeight(width)) : height);
        int uOffset = keepRatio ? Math.negateExact(imageAlignment.getXPos(width, relativeWidth)) : 0;
        int vOffset = keepRatio ? Math.negateExact(imageAlignment.getYPos(height, relativeHeight)) : 0;

        //poseStack.pushTransformation(this.poseStackTransformer.createTransformation(this.objectArea));
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        /*RenderSystem.bindTexture(0);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.SRC_COLOR);
        RenderSystem.setShaderColor(231, 255, 0, 0.2f);
*/
        blit(poseStack, this.renderArea.getXMin(), this.renderArea.getYMin(), uOffset, vOffset, this.renderArea.getWidth(), this.renderArea.getHeight(), relativeWidth, relativeHeight);
        /*
        blit(poseStack, this.renderArea.getXMin(), this.renderArea.getYMin(), uOffset, vOffset, this.renderArea.getWidth(), this.renderArea.getHeight(), relativeWidth, relativeHeight);
        */
        RenderSystem.disableBlend();
        //poseStack.popPose();
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
            else if (!this.getPosition().equals(originalPosition)){
                this.setPosition(originalPosition);
            }
        }
        if(isInCenter) this.setPosition(RenderUtil.getScreenCenter(), Area.Point.CENTER);

        if(poseStackTransformer.isTransformed()){
            //this.rotate(this.poseStackTransformer.getRotation());
        }
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
