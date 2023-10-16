package de.thedead2.progression_reloaded.client.gui.fonts.providers;

import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class TTFProvider implements IFontGlyphProvider {
    private final ByteBuffer fontMemory;
    private final STBTTFontinfo fontInfo;
    private final float overSample;
    private final IntSet skip = new IntArraySet();
    final float shiftX;
    final float shiftY;
    final float pointScale;
    final float ascent;
    private Float glyphHeight;

    public TTFProvider(ByteBuffer fontMemory, STBTTFontinfo fontInfo, float height, float overSample, float shiftX, float shiftY, String skip) {
        this.fontMemory = fontMemory;
        this.fontInfo = fontInfo;
        this.overSample = overSample;
        skip.codePoints().forEach(this.skip::add);
        this.shiftX = shiftX * overSample;
        this.shiftY = shiftY * overSample;
        this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height * overSample);

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);
            IntBuffer intbuffer2 = memorystack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, intbuffer, intbuffer1, intbuffer2);
            this.ascent = (float)intbuffer.get(0) * this.pointScale;
        }

    }

    @Override
    @Nullable
    public IUnbakedGlyph getUnbakedGlyph(int character) {
        IUnbakedGlyph unbakedGlyph = null;
        if(!this.skip.contains(character)) {
            try(MemoryStack memorystack = MemoryStack.stackPush()) {
                int i = STBTruetype.stbtt_FindGlyphIndex(this.fontInfo, character);
                if(i != 0) {
                    IntBuffer intBuffer = memorystack.mallocInt(1);
                    IntBuffer intbuffer1 = memorystack.mallocInt(1);
                    IntBuffer intbuffer2 = memorystack.mallocInt(1);
                    IntBuffer intbuffer3 = memorystack.mallocInt(1);
                    IntBuffer intbuffer4 = memorystack.mallocInt(1);
                    IntBuffer intbuffer5 = memorystack.mallocInt(1);
                    STBTruetype.stbtt_GetGlyphHMetrics(this.fontInfo, i, intbuffer4, intbuffer5);
                    STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(this.fontInfo, i, this.pointScale, this.pointScale, this.shiftX, this.shiftY, intBuffer, intbuffer1, intbuffer2, intbuffer3);
                    float f = (float) intbuffer4.get(0) * this.pointScale;
                    if(glyphHeight == null) glyphHeight = f;
                    int j = intbuffer2.get(0) - intBuffer.get(0);
                    int k = intbuffer3.get(0) - intbuffer1.get(0);
                    if(j > 0 && k > 0) {
                        unbakedGlyph = new UnbakedGlyph(intBuffer.get(0), intbuffer2.get(0), -intbuffer1.get(0), -intbuffer3.get(0), f, this.overSample, (float) intbuffer5.get(0) * this.pointScale, i);
                    }
                    else {
                        unbakedGlyph = (IUnbakedGlyph.ISpaceGlyph) () -> f / this.overSample;
                    }
                }
            }
        }
        return unbakedGlyph;
    }


    @Override
    public void close() {
        this.fontInfo.free();
        MemoryUtil.memFree(this.fontMemory);
    }


    @Override
    public IntSet getSupportedGlyphs() {
        return IntStream.range(0, 65535)
                        .filter((i) -> !this.skip.contains(i))
                        .collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
    }

    @Override
    public float getScalingFactor(float px) {
        return  (px * this.overSample) / glyphHeight;
    }


    public class UnbakedGlyph implements IUnbakedGlyph {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final float advance;
        private final float overSample;
        final int index;

        public UnbakedGlyph(int minWidth, int maxWidth, int minHeight, int maxHeight, float advance, float overSample, float bearingX, int index) {
            this.width = maxWidth - minWidth;
            this.height = minHeight - maxHeight;
            this.advance = advance / overSample;
            this.overSample = overSample;
            this.bearingX = (bearingX + (float) minWidth + TTFProvider.this.shiftX) / TTFProvider.this.overSample;
            this.bearingY = (TTFProvider.this.ascent - (float)minHeight + TTFProvider.this.shiftY) / TTFProvider.this.overSample;
            this.index = index;
        }

        public float getAdvance() {
            return this.advance;
        }


        public BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function) {
            return function.apply(new IGlyphInfo() {
                public int getPixelWidth() {
                    return UnbakedGlyph.this.width;
                }

                public int getPixelHeight() {
                    return UnbakedGlyph.this.height;
                }

                public float getOverSample() {
                    return UnbakedGlyph.this.overSample;
                }

                public float getBearingX() {
                    return UnbakedGlyph.this.bearingX;
                }

                public float getBearingY() {
                    return UnbakedGlyph.this.bearingY;
                }

                public void upload(int pXOffset, int pYOffset) {
                    NativeImage nativeimage = new NativeImage(NativeImage.Format.LUMINANCE, UnbakedGlyph.this.width, UnbakedGlyph.this.height, false);
                    nativeimage.copyFromFont(TTFProvider.this.fontInfo, UnbakedGlyph.this.index, UnbakedGlyph.this.width, UnbakedGlyph.this.height, TTFProvider.this.pointScale, TTFProvider.this.pointScale, TTFProvider.this.shiftX,
                                             TTFProvider.this.shiftY, 0, 0);
                    nativeimage.upload(0, pXOffset, pYOffset, 0, 0, UnbakedGlyph.this.width, UnbakedGlyph.this.height, false, true);
                }

                public boolean isColored() {
                    return false;
                }
            });
        }
    }
}