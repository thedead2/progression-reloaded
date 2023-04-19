package de.thedead2.progression_reloaded.api.gui;

public enum Position {
    TOP(0), BOTTOM(95);

    public int yOffset;

    private Position(int offset) {
        this.yOffset = offset;
    }
}
