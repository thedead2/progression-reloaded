package de.thedead2.progression_reloaded.client.gui.util;

import de.thedead2.progression_reloaded.util.misc.FloatSupplier;


public class Size {

    private FloatSupplier width;

    private FloatSupplier height;


    public Size(float width, float height) {
        this(() -> width, () -> height);
    }


    public Size(FloatSupplier width, FloatSupplier height) {
        this.width = width;
        this.height = height;
    }


    public float getHeight() {
        return this.height.getAsFloat();
    }


    public void setHeight(float height) {
        this.setHeight(() -> height);
    }


    public void setHeight(FloatSupplier height) {
        this.height = height;
    }


    public float getWidth() {
        return this.width.getAsFloat();
    }


    public void setWidth(float width) {
        this.setWidth(() -> width);
    }


    public void setWidth(FloatSupplier width) {
        this.width = width;
    }
}
