package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.api.gui.IProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.animation.*;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class LevelProgressOverlay extends ProgressOverlay {
    private float previousAlpha;
    private final KeyframeAnimation animation = new KeyframeAnimation(0, MathHelper.secondsToTicks(10), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC,
                                                                      new Keyframe(MathHelper.secondsToTicks(0.25f), AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC, 1),
                                                                      new Keyframe(MathHelper.secondsToTicks(8), AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC, 1));

    public LevelProgressOverlay(Area area, LevelDisplayInfo levelDisplayInfo, ProgressBar levelProgressBar, TextureInfo backgroundFrame, ResourceLocation font) {
        super(area, levelDisplayInfo, levelProgressBar, backgroundFrame, font);
    }


    @Override
    public void updateProgress(Class<? extends IProgressOverlay> target, IProgressInfo progressInfo) {
        this.previousAlpha = this.progressBar.getAlpha();
        this.animation.reset();
        super.updateProgress(target, progressInfo);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.animation.animate(this.previousAlpha, 0, this.progressBar::setAlpha);
        this.progressBar.render(poseStack, mouseX, mouseY, partialTick);
    }
}
