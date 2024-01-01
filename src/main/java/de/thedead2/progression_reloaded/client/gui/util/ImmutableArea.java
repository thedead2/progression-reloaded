package de.thedead2.progression_reloaded.client.gui.util;

import de.thedead2.progression_reloaded.util.misc.FloatSupplier;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;


/**
 * An immutable representation of an {@link Area}. Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
 */
public class ImmutableArea extends Area {

    public ImmutableArea(float xPos, float yPos, float zPos, float width, float height) {
        super(xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(Float2FloatFunction xPos, Float2FloatFunction yPos, float zPos, float width, float height, Padding padding) {
        super(xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
        super(xPos, yPos, zPos, width, height, padding);
    }


    public ImmutableArea(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height) {
        super(xPos, yPos, zPos, width, height);
    }


    public ImmutableArea(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos, FloatSupplier width, FloatSupplier height, Padding padding) {
        super(xPos, yPos, zPos, width, height, padding);
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area intersect(Area other) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setY(float yPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setY(FloatSupplier yPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setX(float xPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setX(FloatSupplier xPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPosition(float xPos, float yPos, float zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPostion(FloatSupplier xPos, FloatSupplier yPos, FloatSupplier zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area align(Alignment alignment, Area other) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area alignWithOffset(Alignment alignment, Area other, float xOffset, float yOffset) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setHeight(float height) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setHeight(FloatSupplier height) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setWidth(float width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setWidth(FloatSupplier width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setZ(float zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setZ(FloatSupplier zPos) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveX(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveY(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area moveZ(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area growX(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area growY(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area scaleX(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area scaleY(float amount) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setInnerWidth(float width) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setInnerHeight(float height) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ImmutableArea copy() {
        return new ImmutableArea(this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float padding) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float leftRight, float topBottom) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public Area setPadding(float left, float right, float top, float bottom) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated Any attempt in modifying the position, size or padding of this area will result in an {@link UnsupportedOperationException} to be thrown!
     */
    @Override
    @Deprecated
    public void set(Area other) {
        throw new UnsupportedOperationException();
    }


    /**
     * @deprecated returns this
     */
    @Override
    @Deprecated
    public ImmutableArea toImmutable() {
        return this;
    }


    /**
     * @return a mutable representation of this area
     */
    public Area toMutable() {
        return new Area(this.xPos, this.yPos, this.zPos, this.width, this.height, this.padding);
    }
}
