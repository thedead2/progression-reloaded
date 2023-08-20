package de.thedead2.progression_reloaded.client.gui;

public class Padding {
    public static final Padding NONE = new Padding(0);
    private final int paddingLeft;
    private final int paddingRight;
    private final int paddingTop;
    private final int paddingBottom;

    public Padding(int padding){
        this(padding, padding);
    }
    public Padding(int paddingSides, int paddingTopBottom){
        this(paddingSides, paddingSides, paddingTopBottom, paddingTopBottom);
    }
    public Padding(int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    public int getLeft() {
        return paddingLeft;
    }

    public int getTop() {
        return paddingTop;
    }

    public int getRight() {
        return paddingLeft + paddingRight;
    }

    public int getBottom() {
        return paddingTop + paddingBottom;
    }
}
