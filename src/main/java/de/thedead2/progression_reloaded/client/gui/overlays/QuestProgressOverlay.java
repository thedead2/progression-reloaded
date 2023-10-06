package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;


public class QuestProgressOverlay extends ProgressOverlay {

    public QuestProgressOverlay(Area area, QuestDisplayInfo displayInfo, ProgressBar questProgressBar, TextureInfo backgroundFrame, Font font) {
        super(area, displayInfo, questProgressBar, backgroundFrame, font);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

    }
}
