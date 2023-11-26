package de.thedead2.progression_reloaded.api.gui.animation;

import com.mojang.blaze3d.vertex.PoseStack;


@FunctionalInterface
public interface IAnimationRenderer {

    void render(IAnimation animation, PoseStack poseStack, int mouseX, int mouseY, float partialTick);
}
