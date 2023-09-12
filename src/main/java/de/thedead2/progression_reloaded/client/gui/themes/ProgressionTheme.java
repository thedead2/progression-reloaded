package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;


public class ProgressionTheme {

    private final String locationsPath;

    private final ResourceLocation logo;

    private final ResourceLocation toast;


    public ProgressionTheme(String locationsPath, String logo, String toast) {
        this.locationsPath = locationsPath;
        this.logo = createId(logo);
        this.toast = createId(toast);
    }


    private ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, this.locationsPath + name);
    }
}
