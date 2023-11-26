package de.thedead2.progression_reloaded.client.gui.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimationRenderer;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;


public class RenderableAnimation implements Renderable {

    private final IAnimation animation;

    private final IAnimationRenderer renderer;


    public RenderableAnimation(IAnimation animation, IAnimationRenderer renderer) {
        this.animation = animation;
        this.renderer = renderer;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.animation.startIfNeeded();
        this.renderer.render(this.animation, poseStack, mouseX, mouseY, partialTick);
    }
}
