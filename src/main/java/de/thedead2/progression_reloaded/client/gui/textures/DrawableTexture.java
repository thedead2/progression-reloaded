package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.thedead2.progression_reloaded.api.gui.IDrawableResource;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;


public class DrawableTexture extends ScreenComponent implements IDrawableResource {

    private final TextureInfo textureInfo;

    private final float[] colorShift;

    private Padding textureMask;


    public DrawableTexture(TextureInfo textureInfo, Area area) {
        super(area);
        this.textureInfo = textureInfo;
        this.colorShift = textureInfo.getColorShift();
    }


    public float getRenderWidth() {
        return this.area.getInnerWidth();
    }


    public void setRenderWidth(float width) {
        this.area.setInnerWidth(width);
    }


    public float getRenderHeight() {
        return this.area.getInnerHeight();
    }


    public void setRenderHeight(float height) {
        this.area.setInnerHeight(height);
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


    public DrawableTexture setAlpha(float alpha) {
        this.colorShift[3] = alpha;
        return this;
    }


    @Override
    @Deprecated
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }    @Override
    public void draw(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, this.textureInfo.getTextureLocation());

        float maskLeft = 0, maskTop = 0, maskRight = 0, maskBottom = 0;

        if(this.textureMask != null) {
            maskLeft = this.textureMask.getLeft();
            maskTop = this.textureMask.getTop();
            maskRight = this.textureMask.getRight();
            maskBottom = this.textureMask.getBottom();
        }
        float x = this.area.getInnerX() + maskLeft;
        float y = this.area.getInnerY() + maskTop;
        float z = this.area.getZ();
        float u = this.textureInfo.getU() + maskLeft;
        float v = this.textureInfo.getV() + maskTop;
        float width = this.area.getInnerWidth() - maskRight - maskLeft;
        float height = this.area.getInnerHeight() - maskBottom - maskTop;

        //float f = 1.0F / this.textureInfo.getTextureWidth();
        //float f1 = 1.0F / this.textureInfo.getTextureHeight();
        float f = 1.0F / this.area.getInnerWidth();
        float f1 = 1.0F / this.area.getInnerHeight();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        Matrix4f matrix = poseStack.last().pose();
        bufferbuilder.vertex(matrix, x, y + height, z).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(u * f, (v + height) * f1).endVertex();
        bufferbuilder.vertex(matrix, x + width, y + height, z).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv((u + width) * f, (v + height) * f1).endVertex();
        bufferbuilder.vertex(matrix, x + width, y, z).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv((u + width) * f, v * f1).endVertex();
        bufferbuilder.vertex(matrix, x, y, z).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(u * f, v * f1).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();

        if(ModRenderer.isGuiDebug()) {
            RenderUtil.renderAreaDebug(poseStack, this.area, Color.BLUE.getRGB(), Color.MAGENTA.getRGB());
        }
    }


    public float getX() {
        return this.area.getX();
    }


    public float getWidth() {
        return this.area.getWidth();
    }


    @Override
    public void draw(PoseStack poseStack, float xPos, float yPos, float zPos) {
        this.area.setPosition(xPos, yPos, zPos);
        this.draw(poseStack);
    }


    public void setTextureMask(Padding textureMask) {
        this.textureMask = textureMask;
    }









}