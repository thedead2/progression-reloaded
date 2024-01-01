package de.thedead2.progression_reloaded.client.gui.util;

import org.jetbrains.annotations.NotNull;


public record GradientColor(int red, int green, int blue, int alpha, float stopPercent) implements Comparable<GradientColor> {

    public GradientColor(float red, float green, float blue, float alpha, float stopPercent) {
        this((int) red * 255, (int) green * 255, (int) blue * 255, (int) alpha * 255, stopPercent);
    }


    public GradientColor(int color, float stopPercent) {
        this((color >> 16 & 255), (color >> 8 & 255), (color & 255), (color >> 24 & 255), stopPercent);
    }


    @Override
    public int compareTo(@NotNull GradientColor o) {
        return Float.compare(this.stopPercent, o.stopPercent);
    }
}
