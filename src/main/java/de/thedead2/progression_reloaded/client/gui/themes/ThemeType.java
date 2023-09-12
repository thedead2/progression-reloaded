package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;


public enum ThemeType {
    DEFAULT(0, createId("default"), new ProgressionTheme("textures/gui/themes/default/", "pr_logo_default.png", "default_toast.png")),
    OLD_MAP(1, createId("old_map"), new ProgressionTheme("textures/gui/themes/old_map/", "pr_logo_old_map.png", "old_map_toast.png")),
    FUTURISTIC(2, createId("futuristic"), new ProgressionTheme("textures/gui/themes/futuristic/", "pr_logo_futuristic_no_bg.png", "futuristic_toast.png")),
    CUSTOM(3, createId("custom"), new CustomTheme("theme/", "", ""));


    private final int index;

    private final ResourceLocation id;


    private final ProgressionTheme theme;

    ThemeType(int index, ResourceLocation id, ProgressionTheme theme) {
        this.index = index;
        this.id = id;
        this.theme = theme;
    }


    public static void registerBookThemeTextures() {
        registerTheme(DEFAULT);
        registerTheme(OLD_MAP);
        registerTheme(FUTURISTIC);
        registerTheme(CUSTOM);
    }


    private static void registerTheme(ThemeType theme) {
        ItemProperties.register(ModItems.PROGRESSION_BOOK.get(), theme.getId(), (pStack, pLevel, pEntity, pSeed) -> ConfigManager.THEME.get().getIndex());
    }


    public ResourceLocation getId() {
        return id;
    }


    public int getIndex() {
        return index;
    }


    private static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_theme");
    }


    public ProgressionTheme getTheme() {
        return theme;
    }
}
