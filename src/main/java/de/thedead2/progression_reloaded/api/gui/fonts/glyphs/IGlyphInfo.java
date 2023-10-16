package de.thedead2.progression_reloaded.api.gui.fonts.glyphs;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IGlyphInfo {
    int getPixelWidth();

    int getPixelHeight();

    void upload(int pXOffset, int pYOffset);

    boolean isColored();

    float getOverSample();

    default float getLeft() {
        return this.getBearingX();
    }

    default float getRight() {
        return this.getLeft() + (float)this.getPixelWidth() / this.getOverSample();
    }

    default float getUp() {
        return this.getBearingY();
    }

    default float getDown() {
        return this.getUp() + (float)this.getPixelHeight() / this.getOverSample();
    }

    default float getBearingX() {
        return 0.0F;
    }

    default float getBearingY() {
        return 3.0F;
    }
}