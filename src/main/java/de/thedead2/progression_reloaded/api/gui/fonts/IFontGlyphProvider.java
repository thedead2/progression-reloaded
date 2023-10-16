package de.thedead2.progression_reloaded.api.gui.fonts;

import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.annotation.Nullable;


public interface IFontGlyphProvider extends AutoCloseable {
    float getScalingFactor(float px);
    @Nullable
    IUnbakedGlyph getUnbakedGlyph(int character);
    IntSet getSupportedGlyphs();
    void close();
}
