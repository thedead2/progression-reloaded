package de.thedead2.progression_reloaded.client.gui;

import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.IProgressOverlay;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.components.ProgressCompleteToast;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.overlays.LevelProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;


/**
 * Creates different {@link ScreenComponent}, {@link IProgressOverlay} and {@link net.minecraft.client.gui.screens.Screen} by combining the active {@link ProgressionTheme}
 * and {@link ProgressionLayout} and additional data
 */
public abstract class GuiFactory {

    private static final Supplier<ProgressionTheme> activeTheme = ModClientInstance.getInstance().getModRenderer().getThemeManager().getActiveTheme();

    private static final Supplier<ProgressionLayout> activeLayout = ModClientInstance.getInstance().getModRenderer().getThemeManager().getActiveLayout();


    public static ProgressCompleteToast createPRToast(IDisplayInfo displayInfo, Component title) {
        var layout = activeLayout.get();
        var theme = activeTheme.get();
        return new ProgressCompleteToast(layout.toast(), displayInfo, title, theme.toast(), theme.font());
    }


    public static LevelProgressOverlay createLevelOverlay(LevelDisplayInfo levelDisplayInfo, LevelProgress levelProgress) {
        var layout = activeLayout.get();
        var theme = activeTheme.get();
        return new LevelProgressOverlay(layout.levelProgressOL(), levelDisplayInfo, createLevelOverlayProgressBar(levelProgress), theme.backgroundFrame(), theme.font());
    }


    public static ProgressBar createLevelOverlayProgressBar(LevelProgress progress) {
        var layout = activeLayout.get();
        var theme = activeTheme.get();
        return new ProgressBar(layout.levelProgressOL(), theme.progressBarEmpty(), theme.progressBarFilled(), progress, false, theme.font());
    }
}
