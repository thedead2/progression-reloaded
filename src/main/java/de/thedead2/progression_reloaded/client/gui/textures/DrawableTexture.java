package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.ObjectFit;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;


public class DrawableTexture extends ScreenComponent {

    private final TextureInfo textureInfo;
    private final float[] colorShift;


    public DrawableTexture(TextureInfo textureInfo, Area area) {
        super(area);
        this.textureInfo = textureInfo;
        this.colorShift = textureInfo.getColorShift();
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
    public float getAlpha() {
        return this.colorShift[3];
    }


    @Override
    public DrawableTexture setAlpha(float alpha) {
        this.colorShift[3] = alpha;
        return this;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, this.textureInfo.getTextureLocation());

        final ObjectFit objectFit = this.textureInfo.getObjectFit();
        float xMin = this.area.getInnerX();
        float yMin = this.area.getInnerY();
        float zPos = this.area.getZ();
        float width = this.area.getInnerWidth();
        float height = this.area.getInnerHeight();

        float uMin = objectFit.getUMin(this.textureInfo, this.area); //start-percent of the width of the original image
        float vMin = objectFit.getVMin(this.textureInfo, this.area); //start-percent of the height of the original image

        float xMax = xMin + width;
        float yMax = yMin + height;

        float uMax = objectFit.getUMax(this.textureInfo, this.area); //end-percent of the width of the original image
        float vMax = objectFit.getVMax(this.textureInfo, this.area); //end-percent of the height of the original image

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        Matrix4f matrix = poseStack.last().pose();
        bufferbuilder.vertex(matrix, xMin, yMax, zPos).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(uMin, vMax).endVertex();
        bufferbuilder.vertex(matrix, xMax, yMax, zPos).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(uMax, vMax).endVertex();
        bufferbuilder.vertex(matrix, xMax, yMin, zPos).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(uMax, vMin).endVertex();
        bufferbuilder.vertex(matrix, xMin, yMin, zPos).color(this.colorShift[0], this.colorShift[1], this.colorShift[2], this.colorShift[3]).uv(uMin, vMin).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.textureInfo.getAltText());
    }
}