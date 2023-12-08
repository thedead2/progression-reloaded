package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.client.gui.util.TooltipInfo;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Tooltip extends ScreenComponent {
    //private final IAnimation fadeAnimation = new SimpleAnimation(MathHelper.secondsToTicks(0.25f), MathHelper.secondsToTicks(0.75f), LoopTypes.NO_LOOP, AnimationTypes.EASE_OUT, InterpolationTypes.QUINTIC);

    private final TextBox textBox;

    private final DrawableTexture background;

    private final float maxWidth, maxHeight;


    public Tooltip(TooltipInfo tooltipInfo, @Nullable Component description, float maxWidth, float maxHeight) {
        this(tooltipInfo.background(), description, tooltipInfo.formatting(), maxWidth, maxHeight);
    }


    public Tooltip(TextureInfo background, @Nullable Component description, FontFormatting formatting, float maxWidth, float maxHeight) {
        super(new Area(0, 0, -1, maxWidth, maxHeight, new Padding(5)));
        this.textBox = new TextBox(this.area, new FormattedString((description != null ? description : Component.empty()), formatting));
        this.background = new DrawableTexture(background, this.area);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }


    @Override
    public ScreenComponent setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.textBox.setAlpha(alpha);
        this.background.setAlpha(alpha);
        return this;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.area.setPadding(5);
        this.area.setPosition(mouseX, mouseY, 10);

        float maxX = RenderUtil.getScreenWidth() - 5;
        float maxPossibleWidth = maxX - mouseX;

        this.area.setInnerWidth(Math.min(this.textBox.value.width(), Math.min(maxPossibleWidth, this.maxWidth)));


        float maxY = RenderUtil.getScreenHeight() - 5;
        float maxPossibleHeight = maxY - mouseY;

        this.area.setInnerHeight(Math.min(this.textBox.value.height(maxWidth), Math.min(maxPossibleHeight, this.maxHeight)));


        super.render(poseStack, mouseX, mouseY, partialTick);

        //this.renderHoverEffect(mouseX, mouseY, this.fadeAnimation, 0, 1, 0.5f, this::setAlpha);
        this.textBox.render(poseStack, mouseX, mouseY, partialTick);

        this.area.setPadding(0);
        this.background.draw(poseStack, mouseX, mouseY, -10);
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.textBox.value().toString());
    }
}
