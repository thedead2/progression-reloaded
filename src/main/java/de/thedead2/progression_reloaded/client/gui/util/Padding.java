package de.thedead2.progression_reloaded.client.gui.util;

public class Padding {
    public static final Padding NONE = new Padding(0);
    private final float paddingLeft;

    private final float paddingRight;

    private final float paddingTop;

    private final float paddingBottom;


    public Padding(float padding) {
        this(padding, padding);
    }


    public Padding(float paddingLeftRight, float paddingTopBottom) {
        this(paddingLeftRight, paddingLeftRight, paddingTopBottom, paddingTopBottom);
    }


    public Padding(float paddingLeft, float paddingRight, float paddingTop, float paddingBottom) {
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }


    public float getLeft() {
        return paddingLeft;
    }


    public float getTop() {
        return paddingTop;
    }


    public float getRight() {
        return paddingRight;
    }


    public float getBottom() {
        return paddingBottom;
    }
}
