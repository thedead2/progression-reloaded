package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public abstract class ScreenComponent extends GuiComponent implements Renderable {

    protected final Area area;
    protected float alpha;


    protected ScreenComponent(Area area) {
        this.area = area;
    }


    public Area getArea() {
        return area;
    }

    public abstract ScreenComponent setAlpha(float alpha);

    public float getAlpha(){
        return this.alpha;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(ModRenderer.isGuiDebug()) {
            RenderUtil.renderObjectOutlineDebug(poseStack, this);
        }
    }
}
