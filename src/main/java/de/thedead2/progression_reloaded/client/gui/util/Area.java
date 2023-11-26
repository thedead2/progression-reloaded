package de.thedead2.progression_reloaded.client.gui.util;

import org.joml.Vector3f;


public class Area {

    private Padding padding;

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


    public Vector3f getCenter() {
        return new Vector3f(this.getCenterX(), this.getCenterY(), this.getZ());
    }


    public float getXMax() {
        return this.xPos + this.width;
    }


    public float getYMax() {
        return this.yPos + this.height;
    }


    public Area setPosition(float xPos, float yPos, float zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        return this;
    }


    public float getZ() {
        return zPos;
    }


    public boolean innerContains(float pX, float pY) {
        return pX >= this.getInnerX() && pX <= this.getInnerXMax() && pY >= this.getInnerY() && pY <= this.getInnerYMax();
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


    public Area moveX(float amount) {
        return this.setX(this.xPos + amount);
    }


    public Area setX(float xPos) {
        this.xPos = xPos;
        return this;
    }


    public float getHeight() {
        return this.height;
    }


    public Area moveY(float amount) {
        return this.setY(this.yPos + amount);
    }


    public Area setY(float yPos) {
        this.yPos = yPos;
        return this;
    }


    public boolean contains(float pX, float pY) {
        return pX >= this.xPos && pX <= this.xPos + this.width && pY >= this.yPos && pY <= this.yPos + this.height;
    }


    public Area moveZ(float amount) {
        return this.setZ(this.zPos + amount);
    }


    public Area setZ(float zPos) {
        this.zPos = zPos;
        return this;
    }


    public Area growX(float amount) {
        return this.setWidth(this.width + amount);
    }


    public Area setWidth(float width) {
        this.width = width;
        return this;
    }


    public Area growY(float amount) {
        return this.setHeight(this.height + amount);
    }


    public Area setHeight(float height) {
        this.height = height;
        return this;
    }


    public Area scaleX(float amount) {
        return this.setWidth(this.width * amount);
    }


    public Area scaleY(float amount) {
        return this.setHeight(this.height * amount);
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


    public Area setInnerWidth(float width) {
        return this.setWidth(width + this.padding.getLeft() + this.padding.getRight());
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


    public Area setInnerHeight(float height) {
        return this.setHeight(height + this.padding.getTop() + this.padding.getBottom());
    }


    public Area copy() {
        return new Area(this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    public Area setPadding(float padding) {
        this.padding = new Padding(padding);
        return this;
    }


    public Area setPadding(float leftRight, float topBottom) {
        this.padding = new Padding(leftRight, topBottom);
        return this;
    }


    public Area setPadding(float left, float right, float top, float bottom) {
        this.padding = new Padding(left, right, top, bottom);
        return this;
    }
}
