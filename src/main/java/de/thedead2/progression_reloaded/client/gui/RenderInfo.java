package de.thedead2.progression_reloaded.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class RenderInfo {
    protected int width;
    protected int height;
    protected final float aspectRatio;
    protected final boolean keepRatio;
    protected RelativePosition relativePosition;
    protected Alignment alignment;
    protected Padding padding;
    protected RenderInfo(Alignment alignment, boolean keepRatio, RelativePosition relativePosition) {
        this(relativePosition, keepRatio, alignment, Padding.NONE);
    }
    protected RenderInfo(RelativePosition relativePosition, boolean keepRatio, Alignment alignment, Padding padding) {
        this.relativePosition = relativePosition;
        this.width = this.relativePosition.getWidth();
        this.height = this.relativePosition.getHeight();
        this.aspectRatio = (float) this.width /this.height;
        this.keepRatio = keepRatio;
        this.alignment = alignment;
        this.padding = padding;
        this.setWidth(this.width);
        this.setHeight(this.height);
    }

    public void setWidth(int width){
        width = width != 0 ? width : Math.round(this.getRelativeWidth(this.height));
        this.width = width;
        this.relativePosition.updateWidth(this.width);
    }

    public void setHeight(int height) {
        height = height != 0 ? height : Math.round(this.getRelativeHeight(this.width));
        this.height = height;
        this.relativePosition.updateHeight(this.height);
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    /** Returns the relative width of the object for the given height **/
    public float getRelativeWidth(int height){
        return height*aspectRatio;
    }
    /** Returns the relative height of the object for the given width **/
    public float getRelativeHeight(int width){
        return width/aspectRatio;
    }

    public abstract void render(PoseStack poseStack, /*int screenWidth, int screenHeight,*/ int mouseX, int mouseY, float partialTick);

    public static abstract class Builder<T> {
        protected int width;
        protected int height;
        protected Alignment alignment = Alignment.DEFAULT;

        protected Builder(){}

        public Builder<T> withWidth(int width){
            this.width = width;
            return this;
        }
        public Builder<T> withHeight(int height){
            this.height = height;
            return this;
        }

        public Builder<T> withAlignment(Alignment alignment){
            this.alignment = alignment;
            return this;
        }

        public abstract T build();
    }
}
