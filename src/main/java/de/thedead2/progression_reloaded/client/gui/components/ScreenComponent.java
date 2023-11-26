package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;


public abstract class ScreenComponent extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry {

    protected final Area area;

    protected float alpha = 1;

    protected boolean focused = true;

    private boolean bool = false;


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


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(ModRenderer.isGuiDebug()) {
            RenderUtil.renderObjectOutlineDebug(poseStack, this);
        }
    }


    public void setFocused(boolean focused) {
        this.focused = focused;
    }


    protected void renderHoverEffect(double mouseX, double mouseY, IAnimation animation, float from, float to, float sleepTime, FloatConsumer consumer) {
        if(!bool && this.isMouseOver(mouseX, mouseY)) {
            animation.start();
            animation.sleep(MathHelper.secondsToTicks(0.05f));
            animation.invert(false);
            bool = true;
        }
        else if(bool) {
            animation.sleep(MathHelper.secondsToTicks(sleepTime));
            animation.invert(true);
            bool = false;
        }
        animation.animate(from, to, consumer);
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


    @Override
    public abstract void updateNarration(@NotNull NarrationElementOutput narrationElementOutput);
}
