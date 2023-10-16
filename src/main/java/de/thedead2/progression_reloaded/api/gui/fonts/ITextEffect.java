package de.thedead2.progression_reloaded.api.gui.fonts;

import com.mojang.blaze3d.vertex.VertexConsumer;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import org.joml.Matrix4f;

@FunctionalInterface
public interface ITextEffect {
    void render(BakedFontGlyph whiteGlyph, float scaling, Matrix4f matrix, VertexConsumer buffer, int packedLight);
}
