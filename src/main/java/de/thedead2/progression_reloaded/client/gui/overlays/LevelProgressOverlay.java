package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.NotNull;


public class LevelProgressOverlay extends ProgressOverlay {

    public LevelProgressOverlay(Area area, LevelDisplayInfo levelDisplayInfo, ProgressBar levelProgressBar, TextureInfo backgroundFrame, Font font) {
        super(area, levelDisplayInfo, levelProgressBar, backgroundFrame, font);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        //this.backgroundFrame.draw(poseStack);
        this.progressBar.render(poseStack, mouseX, mouseY, partialTick);
        //this.font.draw(poseStack, this.displayInfo.getTitle(), this.area.getCenterX(), this.area.getCenterY(), Color.WHITE.getRGB());
    }
}
