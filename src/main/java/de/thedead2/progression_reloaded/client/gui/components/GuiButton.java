package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class GuiButton extends ScreenComponent {

    @Nullable
    protected final FormattedString name;

    @Nullable
    protected final DrawableTexture icon;

    protected final int borderColor;

    protected final Action action;


    public GuiButton(Area area, @Nullable FormattedString name, @Nullable DrawableTexture icon, int borderColor, Action action) {
        super(area);
        this.name = name;
        this.icon = icon;
        this.borderColor = borderColor;
        this.action = action;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.isMouseOver(mouseX, mouseY)) {
            this.action.onClick(this);
            return true;
        }
        return false;
    }


    @Override
    public ScreenComponent setAlpha(float alpha) {
        if(this.icon != null) {
            this.icon.setAlpha(alpha);
        }
        if(this.name != null) {
            this.name.formatting().setAlpha(alpha);
        }
        this.alpha = alpha;
        return this;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        float xOffset = 0;
        if(this.icon != null) {
            this.icon.draw(poseStack);
            xOffset = this.icon.getWidth();
        }
        if(this.name != null) {
            this.name.draw(poseStack, this.area.getInnerX() + xOffset, this.area.getInnerY(), this.area.getZ());
        }
        if(this.borderColor != 0) {
            RenderUtil.renderArea(poseStack, this.area, RenderUtil.changeAlpha(this.borderColor, this.alpha), 0);
        }
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.area.contains((float) mouseX, (float) mouseY);
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }


    public interface Action {

        void onClick(GuiButton button);
    }
}
