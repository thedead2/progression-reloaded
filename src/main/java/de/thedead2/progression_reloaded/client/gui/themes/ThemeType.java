package de.thedead2.progression_reloaded.client.gui.themes;

public enum ThemeType {
    FUTURISTIC(new FuturisticTheme()),
    OLD_MAP(new OldMapTheme()),
    DEFAULT(new DefaultTheme()),
    CUSTOM(new CustomTheme());

    private final ProgressionTheme theme;

    ThemeType(ProgressionTheme theme){
        this.theme = theme;
    }

    public ProgressionTheme getTheme() {
        return theme;
    }
}
