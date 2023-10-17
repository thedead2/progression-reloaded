package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Set;

import static de.thedead2.progression_reloaded.client.gui.util.RenderUtil.convertColor;
import static de.thedead2.progression_reloaded.client.gui.util.RenderUtil.storeColorComponents;


public class FontFormatting {
    private float lineHeight, letterSpacing, lineSpacing;
    private Alignment textAlignment;
    @Nullable
    private IAnimation strikeThroughAnimation, underlineAnimation, bgAnimation;

    private final Set<ITextEffect> textEffects = Sets.newHashSet();

    private final int[] color = new int[4], bgColor = new int[4];
    private boolean bold, italic, underlined, strikethrough, obfuscated;


    private FontFormatting() {
        this(8, 0, 3, Alignment.DEFAULT, Color.WHITE.getRGB(), 0, false, false, false, false, false);
    }

    /**
     * @param lineHeight the line height in px
     * @param letterSpacing the letter spacing in px
     * @param lineSpacing the line spacing in px
     * @param textAlignment the alignment of the text inside the drawing area
     * @param color the color of the text
     * **/
    public FontFormatting(int lineHeight, int letterSpacing, int lineSpacing, Alignment textAlignment, int color, int bgColor, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated) {
        this.lineHeight = lineHeight;
        this.letterSpacing = letterSpacing;
        this.lineSpacing = lineSpacing;
        this.textAlignment = textAlignment;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;

        storeColorComponents(this.color, color);
        storeColorComponents(this.bgColor, bgColor);
    }


    public static FontFormatting of(Style style) {
        return new FontFormatting(8, 0, 1, Alignment.DEFAULT, style.getColor() != null ? style.getColor().getValue() : Color.WHITE.getRGB(), 0, style.isBold(), style.isItalic(), style.isUnderlined(), style.isStrikethrough(), style.isObfuscated());
    }


    public static FontFormatting defaultFormatting() {
        return new FontFormatting();
    }

    public FontFormatting setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }


    public boolean isBold() {
        return bold;
    }


    public boolean isItalic() {
        return italic;
    }


    public boolean isStrikethrough() {
        return strikethrough;
    }


    public boolean isUnderlined() {
        return underlined;
    }


    public float getLetterSpacing() {
        return letterSpacing;
    }


    public boolean isObfuscated() {
        return this.obfuscated;
    }


    public int getColor() {
        return convertColor(this.color);
    }

    public int getRed() {
        return this.color[0];
    }
    public int getGreen() {
        return this.color[1];
    }
    public int getBlue() {
        return this.color[2];
    }
    public int getAlpha() {
        return this.color[3];
    }
    public int getBgRed() {
        return this.bgColor[0];
    }
    public int getBgGreen() {
        return this.bgColor[1];
    }
    public int getBgBlue() {
        return this.bgColor[2];
    }
    public int getBgAlpha() {
        return this.bgColor[3];
    }


    public float getLineHeight() {
        return lineHeight;
    }


    public int getBgColor() {
        return convertColor(this.bgColor);
    }


    public FontFormatting setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }


    public FontFormatting setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }


    public FontFormatting setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }


    public FontFormatting setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }


    public FontFormatting setRed(int red) {
        this.color[0] = red;
        return this;
    }
    public FontFormatting setRed(float red) {
        return this.setRed(Math.round(red * 255));
    }
    public FontFormatting setGreen(int green) {
        this.color[1] = green;
        return this;
    }
    public FontFormatting setGreen(float green) {
        return this.setGreen(Math.round(green * 255));
    }
    public FontFormatting setBlue(int blue) {
        this.color[2] = blue;
        return this;
    }
    public FontFormatting setBlue(float blue) {
        return this.setBlue(Math.round(blue * 255));
    }
    public FontFormatting setAlpha(int alpha) {
        this.color[3] = alpha;
        return this;
    }
    public FontFormatting setAlpha(float alpha) {
        return this.setAlpha(Math.round(alpha * 255));
    }

    public FontFormatting setBgRed(int red) {
        this.bgColor[0] = red;
        return this;
    }
    public FontFormatting setBgRed(float red) {
        return this.setBgRed(Math.round(red * 255));
    }
    public FontFormatting setBgGreen(int green) {
        this.bgColor[1] = green;
        return this;
    }
    public FontFormatting setBgBgGreen(float green) {
        return this.setBgGreen(Math.round(green * 255));
    }
    public FontFormatting setBgBlue(int blue) {
        this.bgColor[2] = blue;
        return this;
    }
    public FontFormatting setBgBlue(float blue) {
        return this.setBgBlue(Math.round(blue * 255));
    }

    public FontFormatting setBgAlpha(int alpha) {
        this.bgColor[3] = alpha;
        return this;
    }
    public FontFormatting setBgAlpha(float alpha) {
        return this.setBgAlpha(Math.round(alpha * 255));
    }

    public FontFormatting setColor(int color) {
        storeColorComponents(this.color, color);
        return this;
    }


    public FontFormatting setBgColor(int bgColor) {
        storeColorComponents(this.bgColor, bgColor);
        return this;
    }


    public FontFormatting setBold(boolean bold) {
        this.bold = bold;
        return this;
    }


    public FontFormatting setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }


    public FontFormatting setUnderlined(boolean underlined) {
        this.underlined = underlined;
        return this;
    }


    public FontFormatting setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }


    public float getLineSpacing() {
        return lineSpacing;
    }


    public Alignment getTextAlignment() {
        return textAlignment;
    }


    public @Nullable IAnimation getStrikeThroughAnimation() {
        return strikeThroughAnimation;
    }


    public void setStrikeThroughAnimation(@Nullable IAnimation strikeThroughAnimation) {
        this.strikeThroughAnimation = strikeThroughAnimation;
    }


    public @Nullable IAnimation getUnderlineAnimation() {
        return underlineAnimation;
    }


    public void setUnderlineAnimation(@Nullable IAnimation underlineAnimation) {
        this.underlineAnimation = underlineAnimation;
    }


    public @Nullable IAnimation getBgAnimation() {
        return bgAnimation;
    }


    public void setBgAnimation(@Nullable IAnimation bgAnimation) {
        this.bgAnimation = bgAnimation;
    }

    public FontFormatting addTextEffect(ITextEffect effect) {
        this.textEffects.add(effect);
        return this;
    }


    public ImmutableSet<ITextEffect> getTextEffects() {
        return ImmutableSet.copyOf(this.textEffects);
    }
}
