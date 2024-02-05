package de.thedead2.progression_reloaded.util.helper;

import net.minecraft.util.Mth;
import org.joml.Vector3f;


public abstract class MathHelper {


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
        return clampTo(deg, 360);
    }


    public static float clampTo(float val, float max) {
        return val % max;
    }


    public static Vector3f getIntersectionPoint(Vector3f A, Vector3f AD, Vector3f B, Vector3f BC) {
        AD.normalize();
        BC.normalize();

        Vector3f BA = B.sub(A);
        Vector3f SNV = new Vector3f(BC.y, -BC.x, -BC.z);

        float t = BA.dot(SNV) / AD.dot(SNV);

        return A.add(AD.mul(t));
    }


    public static float degreesToRadian(float deg) {
        return deg * ((float) Math.PI / 180F);
    }


    public static float radianToDegrees(float rad) {
        return rad * (180F / (float) Math.PI);
    }


    public static int secondsToTicks(float seconds) {
        return Math.round(seconds * 20);
    }


    public static long ticksToMillis(float ticks) {
        return secondsToMillis(ticksToSeconds(ticks));
    }


    public static int millisToTicks(long millis) {
        return secondsToTicks(Math.toIntExact(millisToSeconds(millis)));
    }


    public static long ticksToSeconds(float ticks) {
        return (long) (ticks / 20);
    }

    public static long secondsToMillis(long seconds) {
        return seconds * 1000L;
    }


    public static long millisToSeconds(long millis) {
        return millis / 1000L;
    }


    public static int sectionsPosToBlockPos(int sectionsCoord) {
        return sectionsCoord << 4;
    }


    public static float percentOf(float percent, float of) {
        return (Mth.clamp(percent, 0, 100) / 100) * of;
    }
}
