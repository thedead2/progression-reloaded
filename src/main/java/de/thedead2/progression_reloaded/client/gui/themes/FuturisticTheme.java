package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.client.gui.Alignment;
import de.thedead2.progression_reloaded.client.gui.ImageRenderInfo;
import de.thedead2.progression_reloaded.client.gui.Padding;
import de.thedead2.progression_reloaded.client.gui.RelativePosition;
import net.minecraft.resources.ResourceLocation;

import static de.thedead2.progression_reloaded.client.gui.Alignment.*;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class FuturisticTheme extends ProgressionTheme{
    private static final String textures_path = "textures/gui/themes/futuristic/";
    public FuturisticTheme() {
        super(new ImageRenderInfo(3072, 1157, 45, 60, RelativePosition.AnchorPoint.A, CENTERED, Padding.NONE, true, new ResourceLocation(MOD_ID, textures_path + "background.png")), new ResourceLocation(MOD_ID, textures_path + "background_frames.png"), new ResourceLocation(MOD_ID, textures_path + "circle_main.png"));
    }
}
