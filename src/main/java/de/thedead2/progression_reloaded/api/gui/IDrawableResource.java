package de.thedead2.progression_reloaded.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;


public interface IDrawableResource {

    /**
     * Draws this {@link IDrawableResource} to the screen.
     *
     * @param poseStack a {@link PoseStack} for rendering
     **/
    void draw(PoseStack poseStack);

    /**
     * Draws this {@link IDrawableResource} to the screen at the specified position.
     *
     * @param poseStack a {@link PoseStack} for rendering
     * @param xPos      the x position on the screen to render at
     * @param yPos      the y position on the screen to render at
     * @param zPos      the z position on the screen to render at
     **/
    void draw(PoseStack poseStack, float xPos, float yPos, float zPos);
}
