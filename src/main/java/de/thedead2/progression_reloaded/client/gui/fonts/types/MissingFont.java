package de.thedead2.progression_reloaded.client.gui.fonts.types;

import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.SpecialGlyphs;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;


public class MissingFont extends ProgressionFont {

    public MissingFont() {
        super(new ResourceLocation(ModHelper.MOD_ID, "missing_font"), Lists.newArrayList());
    }

    @Override
    public @NotNull IUnbakedGlyph getUnbakedGlyph(int character) {
        return SpecialGlyphs.MISSING;
    }
}
