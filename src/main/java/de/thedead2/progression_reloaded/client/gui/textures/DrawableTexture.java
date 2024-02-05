package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;


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
        RenderUtil.renderImage(poseStack, this.textureInfo, this.area, this.colorShift);
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.textureInfo.getAltText());
    }
}