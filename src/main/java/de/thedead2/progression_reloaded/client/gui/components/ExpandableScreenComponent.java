package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;


public class ExpandableScreenComponent<T extends ScreenComponent> extends ScreenComponent {

    private final SimpleAnimation slideAnimation = new SimpleAnimation(0, 10, LoopTypes.NO_LOOP, AnimationTypes.EASE_OUT, InterpolationTypes.CUBIC, false);

    private final SimpleAnimation rotateAnimation = new SimpleAnimation(0, 10, LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC, false);

    private final ProgressionFont font;

    private final ScreenComponent header;

    private final T content;

    private boolean expanded;


    public ExpandableScreenComponent(Area area, float headerHeight, ProgressionFont font, BiFunction<Area, T, ScreenComponent> headerFactory, Function<Area, T> contentFactory) {
        super(area);
        this.font = font;
        this.content = contentFactory.apply(this.area.copy().growY(-headerHeight).moveY(headerHeight));
        this.header = headerFactory.apply(this.area.copy().setHeight(headerHeight), this.content);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        if(!ModRenderer.isGuiDebug()) {
            enableScissor(Math.round(this.getX() - 1), Math.round(this.getY() - 1), Math.round(this.getXMax() + 1), Math.round(this.getYMax() + 1));
            if(this.content instanceof ScrollableScreenComponent sc) {
                sc.showOverflow(true);
            }
        }

        poseStack.popPose();
        this.header.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.pushPose();

        poseStack.pushPose();
        if(this.expanded || (!this.slideAnimation.isFinished() && this.slideAnimation.isStarted())) {
            this.slideAnimation.animate(this.getY() + this.header.getHeight() - this.content.getHeight(), this.getY() + this.header.getHeight(), t -> this.content.getArea().setY(t));
            this.content.render(poseStack, mouseX, mouseY, partialTick);
        }

        this.rotateAnimation.animate(-90, 0, t -> RenderUtil.rotateAround(poseStack, Axis.ZP.rotationDegrees(t), this.getInnerXMax() - this.font.width("V") / 2, this.getInnerY() + this.font.getLineHeight() / 2, this.getZ()));
        this.font.draw(poseStack, "V", this.getInnerXMax() - this.font.width("V"), this.getInnerY(), this.getZ());

        poseStack.popPose();

        if(!ModRenderer.isGuiDebug()) {
            disableScissor();
        }
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        if(this.expanded) {
            this.content.updateNarration(narrationElementOutput);
        }
    }


    public T getContent() {
        return content;
    }


    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.header.mouseMoved(mouseX, mouseY);
        if(this.expanded) {
            this.content.mouseMoved(mouseX, mouseY);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.header.mouseClicked(mouseX, mouseY, button) || this.header.isMouseOver(mouseX, mouseY)) {
            this.expand();
            return true;
        }
        else if(this.expanded) {
            return this.content.mouseClicked(mouseX, mouseY, button);
        }
        else {
            return false;
        }
    }


    public void expand() {
        this.expanded = !this.expanded;
        this.slideAnimation.invert(!this.expanded);
        this.rotateAnimation.invert(!this.expanded);
        this.slideAnimation.start();
        this.rotateAnimation.start();
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.header.mouseReleased(mouseX, mouseY, button) || (this.expanded && this.content.mouseReleased(mouseX, mouseY, button));
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.header.mouseDragged(mouseX, mouseY, button, dragX, dragY) || (this.expanded && this.content.mouseDragged(mouseX, mouseY, button, dragX, dragY));
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.header.mouseScrolled(mouseX, mouseY, delta) || (this.expanded && this.content.mouseScrolled(mouseX, mouseY, delta));
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.header.keyPressed(keyCode, scanCode, modifiers) || (this.expanded && this.content.keyPressed(keyCode, scanCode, modifiers));
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.header.keyReleased(keyCode, scanCode, modifiers) || (this.expanded && this.content.keyReleased(keyCode, scanCode, modifiers));
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.header.charTyped(codePoint, modifiers) || (this.expanded && this.content.charTyped(codePoint, modifiers));
    }
}
