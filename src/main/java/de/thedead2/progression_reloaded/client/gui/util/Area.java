package de.thedead2.progression_reloaded.client.gui.util;

public class Area {

    private final Padding padding;

    private float xPos;

    private float yPos;

    private float zPos;

    private float width;

    private float height;


    public Area(float xPos, float yPos, float zPos, float width, float height) {
        this(xPos, yPos, zPos, width, height, Padding.NONE);
    }


    public Area(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.width = width;
        this.height = height;
        this.padding = padding;
    }


    public Area intersect(Area other) {
        this.xPos = Math.max(this.xPos, other.getX());
        this.yPos = Math.max(this.yPos, other.getY());
        this.width = Math.max(0, Math.min(this.getXMax(), other.getXMax()) - this.xPos);
        this.height = Math.max(0, Math.min(this.getYMax(), other.getYMax()) - this.yPos);
        return this;
    }


    public float getX() {
        return this.xPos;
    }


    public float getY() {
        return this.yPos;
    }


    public void setY(float yPos) {
        this.yPos = yPos;
    }


    public float getXMax() {
        return this.xPos + this.width;
    }


    public float getYMax() {
        return this.yPos + this.height;
    }


    public void setX(float xPos) {
        this.xPos = xPos;
    }


    public float getZ() {
        return zPos;
    }


    public void setZ(float zPos) {
        this.zPos = zPos;
    }


    public float getCenterX() {
        return this.xPos + this.width / 2;
    }


    public float getCenterY() {
        return this.yPos + this.height / 2;
    }


    public float getWidth() {
        return this.width;
    }


    public void setWidth(float width) {
        this.width = width;
    }


    public float getHeight() {
        return this.height;
    }


    public void setHeight(float height) {
        this.height = height;
    }


    public void setPosition(float xPos, float yPos, float zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }


    public boolean contains(float pX, float pY) {
        return pX >= this.xPos && pX <= this.xPos + this.width && pY >= this.yPos && pY <= this.yPos + this.height;
    }


    public void moveX(float amount) {
        this.setX(this.xPos + amount);
    }


    public void moveY(float amount) {
        this.setY(this.yPos + amount);
    }


    public void moveZ(float amount) {
        this.setZ(this.zPos + amount);
    }


    public void growX(float amount) {
        this.setWidth(this.width + amount);
    }


    public void growY(float amount) {
        this.setHeight(this.height + amount);
    }


    public void scaleX(float amount) {
        this.setWidth(this.width * amount);
    }


    public void scaleY(float amount) {
        this.setHeight(this.height * amount);
    }


    public float getInnerXMax() {
        return this.getInnerX() + this.getInnerWidth();
    }


    public float getInnerX() {
        return this.xPos + this.padding.getLeft();
    }


    public float getInnerWidth() {
        return this.width - this.padding.getLeft() - this.padding.getRight();
    }


    public void setInnerWidth(float width) {
        this.setWidth(width + this.padding.getLeft() + this.padding.getRight());
    }


    public float getInnerYMax() {
        return this.getInnerY() + this.getInnerHeight();
    }


    public float getInnerY() {
        return this.yPos + this.padding.getTop();
    }


    public float getInnerHeight() {
        return this.height - this.padding.getTop() - this.padding.getBottom();
    }


    public void setInnerHeight(float height) {
        this.setHeight(height + this.padding.getTop() + this.padding.getBottom());
    }


    public Area copy() {
        return new Area(this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }
}
