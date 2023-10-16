package de.thedead2.progression_reloaded.client.gui.fonts.providers;

import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.Nullable;


public class SpaceProvider implements IFontGlyphProvider {

    private final Int2ObjectMap<IUnbakedGlyph.ISpaceGlyph> glyphs;

    public SpaceProvider(Int2FloatMap int2FloatMap) {
        this.glyphs = new Int2ObjectOpenHashMap<>(int2FloatMap.size());
        Int2FloatMaps.fastForEach(int2FloatMap, (entry) -> {
            float f = entry.getFloatValue();
            this.glyphs.put(entry.getIntKey(), () -> f);
        });
    }

    @Override
    public float getScalingFactor(float px) {
        return 0;
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


    @Override
    public void close() {
    }
}
