package de.thedead2.progression_reloaded.api.gui.fonts.glyphs;

import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import org.joml.Matrix4f;


@FunctionalInterface
public interface IGlyphTransform {

    void transform(BakedFontGlyph glyph, Matrix4f matrix, float xPos, float yPos, float zPos, float glyphWidth, float glyphHeight);
}
