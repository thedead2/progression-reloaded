package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

public enum ThemeType {
    FUTURISTIC(new ProgressionTheme()),
    OLD_MAP(new ProgressionTheme()),
    DEFAULT(new ProgressionTheme()),
    CUSTOM(new ProgressionTheme());

    private static final String futuristic_path = "textures/gui/themes/futuristic/";
    private static final String old_map_path = "textures/gui/themes/old_map/";
    private static final String default_path = "textures/gui/themes/default/";

    private static ResourceLocation id(String path, String name){
        return new ResourceLocation(ModHelper.MOD_ID, path + name);
    }


    private final ProgressionTheme theme;

    ThemeType(ProgressionTheme theme){
        this.theme = theme;
    }

    public ProgressionTheme getTheme() {
        return theme;
    }
}
