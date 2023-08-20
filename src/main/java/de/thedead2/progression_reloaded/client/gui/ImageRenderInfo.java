package de.thedead2.progression_reloaded.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.gui.GuiComponent.blit;

public class ImageRenderInfo extends RenderInfo {
    private final ResourceLocation texture;

    public ImageRenderInfo(int width, int height, int x, int y, RelativePosition.AnchorPoint anchorPoint, Alignment alignment, Padding padding, ResourceLocation texture) {
        this(width, height, x, y, anchorPoint, alignment, padding, true, texture);
    }
    public ImageRenderInfo(int width, int height, int x, int y, RelativePosition.AnchorPoint anchorPoint, Alignment alignment, Padding padding, boolean keepRatio, ResourceLocation texture) {
        super(new RelativePosition(x, y, width, height, anchorPoint), keepRatio, alignment, padding);
        this.texture = texture;
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick){
        int relativeWidth = (keepRatio ? Math.round(this.getRelativeWidth(height)) : width);
        int relativeHeight = (keepRatio ? Math.round(this.getRelativeHeight(width)) : height);
        RenderSystem.setShaderTexture(0, texture);
        int renderWidth = Math.min(width, relativeWidth);
        blit(poseStack, this.relativePosition.getXMin() + (renderWidth == relativeWidth ? alignment.getXPos(width, relativeWidth) : 0) + this.padding.getLeft(), this.relativePosition.getYMin() + this.padding.getTop(), renderWidth == relativeWidth ? 0 : Math.negateExact(alignment.getXPos(width, relativeWidth)), alignment.getYPos(height, height), renderWidth - this.padding.getRight(), height - this.padding.getBottom(), relativeWidth, height);
    }

    public static class Builder extends RenderInfo.Builder<ImageRenderInfo> {
        private ResourceLocation texture;
        private boolean keepRatio;
        private int paddingRight;
        private int paddingLeft;
        private int paddingUp;
        private int paddingDown;

        public static Builder create(){
            return new Builder();
        }

        public Builder withTexture(ResourceLocation texture){
            this.texture = texture;
            return this;
        }
        public Builder shouldKeepRatio(boolean keepRatio){
            this.keepRatio = keepRatio;
            return this;
        }

        public ImageRenderInfo build(){
            return null; //new ImageRenderInfo(this.width, this.height, this.alignment, new Padding(this.paddingLeft, this.paddingRight, this.paddingUp, this.paddingDown), this.keepRatio, this.texture);
        }
    }
}
