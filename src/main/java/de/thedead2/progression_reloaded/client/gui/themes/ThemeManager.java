package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import de.thedead2.progression_reloaded.client.gui.util.ObjectFit;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ThemeManager {

    private final Supplier<ProgressionLayout> activeLayout = () -> ProgressionLayout.Builder.builder()
                                                                                            .withToast(() -> RenderUtil.getScreenCenter().x - (MathHelper.percentOf(25, RenderUtil.getScreenWidth()) / 2), () -> MathHelper.percentOf(10, RenderUtil.getScreenHeight()), () -> 0, () -> MathHelper.percentOf(25, RenderUtil.getScreenWidth()), () -> MathHelper.percentOf(10, RenderUtil.getScreenHeight()), Padding.NONE)
                                                                                            .withLevelProgressOL(() -> 0, () -> 0, () -> 0, () -> MathHelper.percentOf(10, RenderUtil.getScreenWidth()), () -> MathHelper.percentOf(3, RenderUtil.getScreenHeight()), new Padding(5))
                                                                                            .withQuestProgressOL(() -> RenderUtil.getScreenWidth() - 50, () -> 50, () -> 0, () -> 50, () -> 150, new Padding(5))
                                                                                            .build();

    private final Supplier<ProgressionTheme> activeTheme = () -> ProgressionTheme.Builder.builder("textures/gui/themes/futuristic/")
                                                                                         .withOrdinal(0)
                                                                                         .withToast("futuristic_toast.png", Component.empty(), 0, 0, 3072, 382, ObjectFit.CONTAIN)
                                                                                         .withFont(new ResourceLocation(ModHelper.MOD_ID, "expansiva"))
                                                                                         .withLogo("pr_logo_futuristic_no_bg.png", Component.empty(), 0, 0, 3072, 2069, ObjectFit.CONTAIN)
                                                                                         .withBackgroundFrame("bg_frame.png", Component.empty(), 0, 0, 3072, 1956, ObjectFit.FILL)
                                                                                         .withQuestWidget("quest_widget_hovered.png", Component.empty(), 0, 0, 3072, 3091, ObjectFit.CONTAIN)
                                                                                         .withProgressBarEmpty("progress_bar_empty.png", Component.empty(), 0, 0, 3072, 105, ObjectFit.COVER)
                                                                                         .withProgressBarFilled("progress_bar_filled.png", Component.empty(), 0, 0, 3072, 247, ObjectFit.COVER)
                                                                                         .withTooltip("tooltip_frame.png", Component.empty(), 0, 0, 3072, 895, ObjectFit.FILL, FontFormatting.defaultFormatting().setLineHeight(3))
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
