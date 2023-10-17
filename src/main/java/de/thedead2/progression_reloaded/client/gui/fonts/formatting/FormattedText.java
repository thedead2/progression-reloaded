package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class FormattedText {
    private final String text;
    private final ResourceLocation font;
    private final FontFormatting formatting;
    private final boolean withShadow;

    public FormattedText(Component text, ResourceLocation font, FontFormatting formatting, boolean withShadow) {
        this(text.getString(), font, formatting, withShadow);
    }
    public FormattedText(String text, ResourceLocation font, FontFormatting formatting, boolean withShadow) {
        this.text = text;
        this.font = font;
        this.formatting = formatting;
        this.withShadow = withShadow;
    }


    public String text() {
        return text;
    }


    public ResourceLocation font() {
        return font;
    }


    public FontFormatting formatting() {
        return formatting;
    }


    public boolean withShadow() {
        return withShadow;
    }


    public float width() {
        ProgressionFont font1 = FontManager.getFont(this.font).format(this.formatting);
        return font1.width(this.text);
    }


    public float height(float maxWidth) {
        ProgressionFont font1 = FontManager.getFont(this.font).format(this.formatting);
        return font1.height(this.text, maxWidth);
    }


    public FormattedText subString(int start, int end) {
        String sub = this.text.substring(start, end);
        return new FormattedText(sub, this.font, this.formatting, this.withShadow);
    }


    public boolean isEmpty() {
        return this.text.isEmpty();
    }
}
