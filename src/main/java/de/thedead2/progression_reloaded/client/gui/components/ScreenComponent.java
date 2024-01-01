package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;


public abstract class ScreenComponent extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry {

    protected final Area area;

    protected float alpha = 1;

    protected boolean focused = false;


    @Override
    public boolean changeFocus(boolean focus) {
        this.focused = focus;

        return this.focused;
    }


    public Area getArea() {
        return area;
    }


    public ScreenComponent(Area area) {
        this.area = area;
    }

    public float getAlpha(){
        return this.alpha;
    }


    public ScreenComponent setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }


    public ScreenComponent align(Alignment alignment, @Nullable Area other) {
        return this.alignWithOffset(alignment, other, 0, 0);
    }


    public ScreenComponent alignWithOffset(Alignment alignment, @Nullable Area other, float xOffset, float yOffset) {
        this.area.alignWithOffset(alignment, other, xOffset, yOffset);

        return this;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(ModRenderer.isGuiDebug()) {
            RenderUtil.renderObjectOutlineDebug(poseStack, this);
        }
    }


    public boolean isFocused() {
        return focused;
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.area.contains((float) mouseX, (float) mouseY);
    }


    @Override
    public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
        if(this.focused) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        else {
            return this.isMouseOver() ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }


    public boolean isMouseOver() {
        Vector2d mousePos = RenderUtil.getMousePos();
        return this.isMouseOver(mousePos.x, mousePos.y);
    }


    public float getY() {
        return this.area.getY();
    }
    public float getZ() {
        return this.area.getZ();
    }


    public float getWidth() {
        return this.area.getWidth();
    }


    public float getHeight() {
        return this.area.getHeight();
    }


    public float getInnerWidth() {
        return this.area.getInnerWidth();
    }


    public void setInnerWidth(float width) {
        this.area.setInnerWidth(width);
    }


    public float getInnerHeight() {
        return this.area.getInnerHeight();
    }


    public float getInnerX() {
        return this.area.getInnerX();
    }

    @Override
    public abstract void updateNarration(@NotNull NarrationElementOutput narrationElementOutput);


    public float getInnerY() {
        return this.area.getInnerY();
    }


    public float getX() {
        return this.area.getX();
    }


    public float getXMax() {
        return this.area.getXMax();
    }


    public float getInnerXMax() {
        return this.area.getInnerXMax();
    }


    public float getYMax() {
        return this.area.getYMax();
    }
}
