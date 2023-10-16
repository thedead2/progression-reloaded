package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedText;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;


public class ProgressCompleteToast extends ScreenComponent {

    private final IDisplayInfo displayInfo;

    private final DrawableTexture toastTexture;
    private final Component title;
    private final Alignment toastAlignment = Alignment.CENTERED;

    private final IAnimation animation;
    private final TextBox textBox;
    
    public ProgressCompleteToast(Area area, IDisplayInfo displayInfo, Component title, TextureInfo toastTexture, ResourceLocation font) {
        super(area);
        this.displayInfo = displayInfo;
        this.title = title;
        this.toastTexture = new DrawableTexture(toastTexture, this.area);
        this.animation = new SimpleAnimation(0, MathHelper.secondsToTicks(4), LoopTypes.LOOP_TIMES_INVERSE(1), AnimationTypes.EASE_IN_OUT, InterpolationTypes.EXPONENTIAL).pause(true);

        this.textBox = new TextBox(this.area, List.of(new FormattedText(title, font, new FontFormatting().setLineHeight(4), true),
                                                      new FormattedText(displayInfo.getTitle(), font, new FontFormatting().setLineHeight(10).setLetterSpacing(2).setTextAlignment(Alignment.TOP_CENTERED), true)
        ));
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.pushPose();
        this.area.setPosition(this.toastAlignment.getXPos(0, RenderUtil.getScreenWidth(), this.area.getWidth(), 0), this.toastAlignment.getYPos(0, RenderUtil.getScreenHeight(), this.area.getHeight(), -60), -500);
        this.animation.pause(false).animate(0, 1, this::setAlpha);
        this.toastTexture.draw(poseStack);
        this.textBox.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
    }


    public boolean shouldRender() {
        return !this.animation.isFinishedAndNotLooping();
    }


    @Override
    public ProgressCompleteToast setAlpha(float alpha) {
        this.toastTexture.setAlpha(alpha);
        this.textBox.setAlpha(alpha);
        this.alpha = alpha;
        return this;
    }
}
