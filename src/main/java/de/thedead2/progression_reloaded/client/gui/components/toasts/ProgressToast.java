package de.thedead2.progression_reloaded.client.gui.components.toasts;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ProgressToast extends NotificationToast {
    private final DrawableTexture toastTexture;
    private final TextBox textBox;


    public ProgressToast(Area area, IDisplayInfo<?> displayInfo, Component title, TextureInfo toastTexture, ResourceLocation font) {
        super(area, title, Alignment.CENTERED, new SimpleAnimation(0, MathHelper.secondsToTicks(1), LoopTypes.LOOP_TIMES_INVERSE(1), AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC, false));
        this.toastTexture = new DrawableTexture(toastTexture, this.area);

        this.textBox = new TextBox(this.area, List.of(
                new FormattedString(title, font, FontFormatting.defaultFormatting().setLineHeight(4), true),
                new FormattedString(displayInfo.getTitle(), font, FontFormatting.defaultFormatting().setLineHeight(10).setLetterSpacing(2).setTextAlignment(Alignment.TOP_CENTERED), true)
        ));
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.area.setPosition(this.toastAlignment.getXPos(0, RenderUtil.getScreenWidth(), this.area.getWidth(), 0), this.toastAlignment.getYPos(0, RenderUtil.getScreenHeight(), this.area.getHeight(), -60), -500);

        this.animation.startIfNeeded()
                      .animate(0, 1, this::setAlpha)
                      .sleepIf(IAnimation::isFinishedButLooping, MathHelper.secondsToTicks(4));

        this.toastTexture.draw(poseStack);
        this.textBox.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    public ProgressToast setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.toastTexture.setAlpha(alpha);
        this.textBox.setAlpha(alpha);
        return this;
    }
}
