package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class QuestProgressOverlay extends ProgressOverlay<ProgressionQuest> {

    public QuestProgressOverlay(Area area, QuestDisplayInfo displayInfo, ProgressBar<ProgressionQuest> questProgressBar, TextureInfo backgroundFrame, ResourceLocation font) {
        super(area, displayInfo, questProgressBar, backgroundFrame, font);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.backgroundFrame.draw(poseStack);
        this.progressBar.render(poseStack, mouseX, mouseY, partialTick);
    }


    public boolean isQuestFollowed(ResourceLocation questId) {
        return this.displayInfo.getId().equals(questId);
    }
}
