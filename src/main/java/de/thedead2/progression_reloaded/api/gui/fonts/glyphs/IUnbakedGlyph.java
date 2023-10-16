package de.thedead2.progression_reloaded.api.gui.fonts.glyphs;

import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;


public interface IUnbakedGlyph {
    BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function);
    float getAdvance();

    default float getAdvance(boolean bold) {
        return this.getAdvance() + (bold ? this.getBoldOffset() : 0.0F);
    }

    default float getBoldOffset() {
        return 1.0F;
    }

    default float getShadowOffset() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    interface ISpaceGlyph extends IUnbakedGlyph {
        @Override
        default BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function) {
            return BakedFontGlyph.EmptyFontGlyph.INSTANCE;
        }
    }
}
