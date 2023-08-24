package de.thedead2.progression_reloaded.client.gui.util;

import com.mojang.math.Transformation;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class PoseStackTransformer {
    public static final PoseStackTransformer NONE = new PoseStackTransformer();
    private final double rotationDegrees;
    private final RotationType rotationType;

    public PoseStackTransformer() {
        this(0, RotationType.NONE);
    }

    public PoseStackTransformer(double rotationDegrees, RotationType rotationType) {
        this.rotationDegrees = rotationDegrees;
        this.rotationType = rotationType;
    }

    public Quaternionf getRotation() {
        return new Quaternionf(new AxisAngle4d(Math.toRadians(rotationDegrees), new Vector3f(0, 0, 1)));
    }

    public boolean isLeftRotation(){
        return this.rotationType == RotationType.LEFT;
    }
    public boolean isRightRotation(){
        return this.rotationType == RotationType.RIGHT;
    }

    public Vector3f getScaleVector() {
        return new Vector3f(1);
    }

    @Nullable
    public Vector3f getTranslation(Area objectArea) {
        return null; //new Vector3f(RenderUtil.getScreenCenter(), 0);
        /*if(rotationDegrees == 180) return new Vector3f(RenderUtil.getScreenCenter(), 0);
        return null;*/
    }

    public Transformation createTransformation(Area objectArea) {
        return new Transformation(this.getTranslation(objectArea), isLeftRotation() ? getRotation() : null, getScaleVector(), isRightRotation() ? getRotation() : null);
    }

    public boolean isTransformed() {
        return this != NONE;
    }

    enum RotationType{
        LEFT,
        RIGHT,
        NONE
    }
}
