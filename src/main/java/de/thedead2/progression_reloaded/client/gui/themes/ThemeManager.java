package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.util.ConfigManager;


public class ThemeManager {

    public ProgressionTheme getActiveTheme() {
        return ConfigManager.THEME.get().getTheme();
    }
}
