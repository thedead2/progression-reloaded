package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ThemeManager {

    private final Supplier<ProgressionLayout> activeLayout = () -> ProgressionLayout.Builder.builder()
                                                                                            .withToast(200, 100, 0, 150, 50, Padding.NONE)
                                                                                            .withLevelProgressOL(0, 0, 0, 100, 13, new Padding(5))
                                                                                            .build();

    private final Supplier<ProgressionTheme> activeTheme = () -> ProgressionTheme.Builder.builder("textures/gui/themes/futuristic/")
                                                                                         .withOrdinal(0)
                                                                                         .withToast("futuristic_toast.png", 0, 0, 3072, 382)
                                                                                         .withFont(new ResourceLocation(ModHelper.MOD_ID, "expansiva"))
                                                                                         .withLogo("pr_logo_futuristic_no_bg.png", 0, 0, 3072, 2069)
                                                                                         .withBackgroundFrame("bg_frame.png", 0, 0, 3072, 1381)
                                                                                         .withProgressBarEmpty("progress_bar_empty.png", 0, 0, 3072, 105)
                                                                                         .withProgressBarFilled("progress_bar_filled.png", 0, 0, 3072, 247)
                                                                                         .withLayout(new ResourceLocation(ModHelper.MOD_ID, "futuristic_layout"))
                                                                                         .build();

    private final Map<ResourceLocation, ProgressionTheme> themes = new HashMap<>();

    private final Map<ResourceLocation, ProgressionLayout> layouts = new HashMap<>();


    public ThemeManager() {
        this.registerThemes();
        this.registerLayouts();
    }


    private void registerThemes() {
        this.themes.putAll(PREventFactory.onThemeRegistration());
        this.themes.keySet().forEach(this::registerTheme);
    }


    private void registerLayouts() {
        this.layouts.putAll(PREventFactory.onLayoutRegistration());
    }


    private void registerTheme(ResourceLocation theme) {
        ItemProperties.register(ModItems.PROGRESSION_BOOK.get(), theme, (pStack, pLevel, pEntity, pSeed) -> this.activeTheme.get().ordinal());
    }


    private static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_theme");
    }


    @Nullable
    public ProgressionLayout getLayout(ResourceLocation id) {
        return this.layouts.get(id);
    }


    @Nullable
    public ProgressionTheme getTheme(ResourceLocation id) {
        return this.themes.get(id);
    }


    public Supplier<ProgressionTheme> getActiveTheme() {
        return activeTheme;
    }


    public Supplier<ProgressionLayout> getActiveLayout() {
        return activeLayout;
    }
}
