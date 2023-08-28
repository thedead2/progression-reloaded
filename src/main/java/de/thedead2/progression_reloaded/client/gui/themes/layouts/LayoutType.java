package de.thedead2.progression_reloaded.client.gui.themes.layouts;

public enum LayoutType {
    FUTURISTIC(),
    OLD_MAP(),
    DEFAULT(),
    CUSTOM();

    private final ThemeLayout layout;


    LayoutType() {
        this.layout = null;
    }


    public ThemeLayout getLayout() {
        return layout;
    }
}
