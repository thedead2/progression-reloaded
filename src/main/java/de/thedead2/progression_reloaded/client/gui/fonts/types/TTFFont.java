package de.thedead2.progression_reloaded.client.gui.fonts.types;

import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;


public class TTFFont extends ProgressionFont {

    private final ProgressionFont bold_font;

    private final ProgressionFont italic_font;

    private final ProgressionFont bold_italic_font;


    public TTFFont(ResourceLocation name, List<IFontGlyphProvider> glyphProviders, ProgressionFont boldFont, ProgressionFont italicFont, ProgressionFont boldItalicFont) {
        super(name, glyphProviders);
        bold_font = boldFont;
        italic_font = italicFont;
        bold_italic_font = boldItalicFont;
    }
}
