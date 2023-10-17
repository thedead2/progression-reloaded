package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.thedead2.progression_reloaded.api.gui.IDrawableResource;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;


public class DrawableTexture implements IDrawableResource {

    private final TextureInfo textureInfo;

    private final Area renderArea;

    private final float[] colorShift = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    private Padding textureMask;


    public DrawableTexture(TextureInfo textureInfo, Area renderArea) {
        this.textureInfo = textureInfo;
        this.renderArea = renderArea;
    }


    public float getRenderWidth() {
        return this.renderArea.getInnerWidth();
    }


    public void setRenderWidth(float width) {
        this.renderArea.setInnerWidth(width);
    }


    public float getRenderHeight() {
        return this.renderArea.getInnerHeight();
    }


    public void setRenderHeight(float height) {
        this.renderArea.setInnerHeight(height);
    }


    public void setRed(float red) {
        this.colorShift[0] = red;
    }


    public void setGreen(float green) {
        this.colorShift[1] = green;
    }

    public void setBlue(float blue) {
        this.colorShift[2] = blue;
    }

    @Override
    public void draw(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.textureInfo.getTextureLocation());
        RenderSystem.setShaderColor(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]);

        float maskLeft = 0, maskTop = 0, maskRight = 0, maskBottom = 0;

        if(this.textureMask != null) {
            maskLeft = this.textureMask.getLeft();
            maskTop = this.textureMask.getTop();
            maskRight = this.textureMask.getRight();
            maskBottom = this.textureMask.getBottom();
        }
        float x = this.renderArea.getInnerX() + maskLeft;
        float y = this.renderArea.getInnerY() + maskTop;
        float z = this.renderArea.getZ();
        float u = this.textureInfo.getU() + maskLeft;
        float v = this.textureInfo.getV() + maskTop;
        float width = this.renderArea.getInnerWidth() - maskRight - maskLeft;
        float height = this.renderArea.getInnerHeight() - maskBottom - maskTop;
        //float f = 1.0F / this.textureInfo.getTextureWidth();
        //float f1 = 1.0F / this.textureInfo.getTextureHeight();
        float f = 1.0F / this.renderArea.getInnerWidth();
        float f1 = 1.0F / this.renderArea.getInnerHeight();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX); //TODO: Use POSITION_TEX_COLOR with .color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3])
        Matrix4f matrix = poseStack.last().pose();
        bufferbuilder.vertex(matrix, x, y + height, z)/*.color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3])*/.uv(u * f, (v + height) * f1).endVertex();
        bufferbuilder.vertex(matrix, x + width, y + height, z)/*.color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3])*/.uv((u + width) * f, (v + height) * f1).endVertex();
        bufferbuilder.vertex(matrix, x + width, y, z)/*.color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3])*/.uv((u + width) * f, v * f1).endVertex();
        bufferbuilder.vertex(matrix, x, y, z)/*.color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3])*/.uv(u * f, v * f1).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();
    }


    public void setAlpha(float alpha) {
        this.colorShift[3] = alpha;
    }


    @Override
    public void draw(PoseStack poseStack, float xPos, float yPos, float zPos) {
        this.renderArea.setPosition(xPos, yPos, zPos);
        this.draw(poseStack);
    }


    public void setTextureMask(Padding textureMask) {
        this.textureMask = textureMask;
    }


    public float getX() {
        return this.renderArea.getX();
    }


    public Area getArea() {
        return this.renderArea;
    }






}