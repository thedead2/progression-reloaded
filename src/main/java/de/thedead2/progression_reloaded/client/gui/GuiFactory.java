package de.thedead2.progression_reloaded.client.gui;

import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ThemeLayout;
import de.thedead2.progression_reloaded.client.gui.util.objects.RenderObject;


public abstract class GuiFactory {

    public static <T extends RenderObject> T create(Class<T> identifier, ProgressionTheme theme, ThemeLayout layout) {
        return null;
    }
}
