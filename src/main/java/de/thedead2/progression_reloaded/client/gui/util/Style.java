package de.thedead2.progression_reloaded.client.gui.util;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Style implements Renderable {

    private final Area area;

    @Nullable
    private Renderable background;


    public Style(Area area) {this.area = area;}


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.background != null) {
            this.background.render(poseStack, mouseX, mouseY, partialTick);
        }
    }


    public void backgroundColor(int color) {
        this.background = (pPoseStack, pMouseX, pMouseY, pPartialTick) -> RenderUtil.fill(pPoseStack, this.area.getX(), this.area.getXMax(), this.area.getY(), this.area.getYMax(), this.area.getZ() - 500, color);
    }


    public void background(GradientType gradientType, float degrees, GradientColor... colors) {
        this.background = switch(gradientType) {
            case LINEAR -> (pPoseStack, pMouseX, pMouseY, pPartialTick) -> RenderUtil.linearGradient(pPoseStack, this.area.getX(), this.area.getXMax(), this.area.getY(), this.area.getYMax(), this.area.getZ() - 500, degrees, colors);
            case RADIAL -> (pPoseStack, pMouseX, pMouseY, pPartialTick) -> RenderUtil.radialGradient(pPoseStack, this.area.getX(), this.area.getXMax(), this.area.getY(), this.area.getYMax(), this.area.getZ() - 500, colors);
        };
    }


    public void backgroundImage(TextureInfo texture) {
        this.background = new DrawableTexture(texture, this.area);
    }


    public enum GradientType {
        LINEAR,
        RADIAL
    }
}
