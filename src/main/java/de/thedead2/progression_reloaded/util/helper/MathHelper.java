package de.thedead2.progression_reloaded.util.helper;

import org.joml.Matrix3f;
import org.joml.Vector3f;


public abstract class MathHelper {

    public static Matrix3f xRotMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix3f(1, 0, 0, 0, cos, -sin, 0, sin, cos);
    }


    public static Matrix3f yRotMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix3f(cos, 0, sin, 0, 1, 0, -sin, 0, cos);
    }


    public static Matrix3f zRotMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Matrix3f(cos, -sin, 0, sin, cos, 0, 0, 0, 1);
    }


    /**
     * Checks if the given rotation in degrees is in between the minDeg and maxDeg range
     *
     * @param rotDeg The rotation to check.
     * @param minDeg The lower limit.
     * @param maxDeg The upper limit.
     *
     * @return Whether the given rotation is between minDeg and maxDeg.
     **/
    public static boolean isRotationBetween(float rotDeg, float minDeg, float maxDeg) {
        minDeg = wrapDegrees(minDeg);
        maxDeg = wrapDegrees(maxDeg);
        rotDeg = wrapDegrees(rotDeg);

        if(maxDeg < minDeg) {
            return 0 <= rotDeg && rotDeg <= maxDeg || minDeg <= rotDeg && rotDeg <= 360;
        }
        else {
            return minDeg <= rotDeg && rotDeg <= maxDeg;
        }
    }


    /**
     * Reduces the given angle to an angle between 0 and 360 degrees.
     */
    public static float wrapDegrees(float deg) {
        return deg % 360;
    }


    public static Vector3f getIntersectionPoint(Vector3f A, Vector3f AD, Vector3f B, Vector3f BC) {
        AD.normalize();
        BC.normalize();

        Vector3f BA = B.sub(A);
        Vector3f SNV = new Vector3f(BC.y, -BC.x, -BC.z);

        float t = BA.dot(SNV) / AD.dot(SNV); //dot --> skalarprodukt

        return A.add(AD.mul(t));
    }


    public static float degreesToRadian(float deg) {
        return deg * ((float) Math.PI / 180F);
    }


    public static float radianToDegrees(float rad) {
        return rad * (180F / (float) Math.PI);
    }


    public static int secondsToTicks(int seconds) {
        return seconds * 20;
    }


    public static int ticksToSeconds(int ticks) {
        return ticks / 20;
    }


    public static long secondsToMillis(long seconds) {
        return seconds * 1000L;
    }
}
