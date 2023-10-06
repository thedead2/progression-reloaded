package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.IProgressOverlay;
import org.jetbrains.annotations.NotNull;


public class CombinedProgressOverlay implements IProgressOverlay {

    private final LevelProgressOverlay levelProgressOverlay;

    private final QuestProgressOverlay questProgressOverlay;


    public CombinedProgressOverlay(LevelProgressOverlay levelProgressOverlay, QuestProgressOverlay questProgressOverlay) {
        this.levelProgressOverlay = levelProgressOverlay;
        this.questProgressOverlay = questProgressOverlay;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.levelProgressOverlay.render(poseStack, mouseX, mouseY, partialTick);
        this.questProgressOverlay.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    public void updateProgress(Class<? extends IProgressOverlay> target, IProgressInfo progressInfo) {
        this.levelProgressOverlay.updateProgress(target, progressInfo);
        this.questProgressOverlay.updateProgress(target, progressInfo);
    }


    @Override
    public void updateDisplayInfo(Class<? extends IProgressOverlay> target, IDisplayInfo displayInfo) {
        this.levelProgressOverlay.updateDisplayInfo(target, displayInfo);
        this.questProgressOverlay.updateDisplayInfo(target, displayInfo);
    }
}
