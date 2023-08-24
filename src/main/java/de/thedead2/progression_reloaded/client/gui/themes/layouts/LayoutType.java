package de.thedead2.progression_reloaded.client.gui.themes.layouts;

public enum LayoutType {
    FUTURISTIC(new FuturisticLayout()),
    OLD_MAP(new OldMapLayout()),
    DEFAULT(new DefaultLayout()),
    CUSTOM(new CustomLayout());

    private final ThemeLayout layout;

    LayoutType(ThemeLayout layout) {
        this.layout = layout;
    }

    public ThemeLayout getLayout() {
        return layout;
    }
}
