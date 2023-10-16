package de.thedead2.progression_reloaded.client.gui.fonts.providers;

import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


public class BitmapProvider implements IFontGlyphProvider {

    private final NativeImage image;
    private final Int2ObjectMap<UnbakedGlyph> glyphs;
    private final int glyphHeight;
    private final float glyphScale;

    public BitmapProvider(NativeImage image, Int2ObjectMap<UnbakedGlyph> glyphs) {
        this.image = image;
        this.glyphs = glyphs;
        UnbakedGlyph glyph = this.glyphs.values().stream().findAny().orElseThrow();
        this.glyphHeight = glyph.height;
        this.glyphScale = glyph.scale();
    }


    @Override
    public void close() {
        this.image.close();
    }


    @Override
    public float getScalingFactor(float px) {
        return (px * (1.0F / this.glyphScale)) / this.glyphHeight;
    }


    @Nullable
    @Override
    public IUnbakedGlyph getUnbakedGlyph(int character) {
        return this.glyphs.get(character);
    }


    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    public record UnbakedGlyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements IUnbakedGlyph {
        public float getAdvance() {
            return (float)this.advance;
        }


        public BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function) {
            return function.apply(new IGlyphInfo() {
                public float getOverSample() {
                    return 1.0F / UnbakedGlyph.this.scale;
                }

                public int getPixelWidth() {
                    return UnbakedGlyph.this.width;
                }

                public int getPixelHeight() {
                    return UnbakedGlyph.this.height;
                }

                public float getBearingY() {
                    return IGlyphInfo.super.getBearingY() + 7.0F - (float) UnbakedGlyph.this.ascent;
                }

                public void upload(int pXOffset, int pYOffset) {
                    UnbakedGlyph.this.image.upload(0, pXOffset, pYOffset, UnbakedGlyph.this.offsetX, UnbakedGlyph.this.offsetY, UnbakedGlyph.this.width, UnbakedGlyph.this.height, false, false);
                }

                public boolean isColored() {
                    return UnbakedGlyph.this.image.format().components() > 1;
                }
            });
        }
    }
}
