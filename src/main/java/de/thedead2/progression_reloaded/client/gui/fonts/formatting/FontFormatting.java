package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphTransform;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Set;

import static de.thedead2.progression_reloaded.client.gui.util.RenderUtil.convertColor;
import static de.thedead2.progression_reloaded.client.gui.util.RenderUtil.storeColorComponents;


public class FontFormatting {

    private ResourceLocation font;
    private float lineHeight, letterSpacing, lineSpacing;
    private Alignment textAlignment;
    @Nullable
    private IAnimation strikethroughAnimation, underlineAnimation, bgAnimation;

    @Nullable
    private IGlyphTransform glyphTransform;

    private final Set<ITextEffect> textEffects = Sets.newHashSet();

    private final int[] color = new int[4], bgColor = new int[4];

    private boolean bold, italic, underlined, strikethrough, obfuscated, withShadow;


    private FontFormatting() {
        this(new ResourceLocation("default"), 8, 0, 3, Alignment.DEFAULT, Color.WHITE.getRGB(), 0, false, false, false, false, false, false);
    }

    /**
     * @param lineHeight the line height in px
     * @param letterSpacing the letter spacing in px
     * @param lineSpacing the line spacing in px
     * @param textAlignment the alignment of the text inside the drawing area
     * @param color the color of the text
     * **/
    public FontFormatting(ResourceLocation font, float lineHeight, float letterSpacing, float lineSpacing, Alignment textAlignment, int color, int bgColor, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated, boolean withShadow) {
        this.font = font;
        this.lineHeight = lineHeight;
        this.letterSpacing = letterSpacing;
        this.lineSpacing = lineSpacing;
        this.textAlignment = textAlignment;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.withShadow = withShadow;

        storeColorComponents(this.color, color);
        storeColorComponents(this.bgColor, bgColor);
    }


    public static FontFormatting of(Style style) {
        return new FontFormatting(style.getFont(), 8, 0, 1, Alignment.DEFAULT, style.getColor() != null ? style.getColor().getValue() : Color.WHITE.getRGB(), 0, style.isBold(), style.isItalic(), style.isUnderlined(), style.isStrikethrough(), style.isObfuscated(), false);
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


    public static FontFormatting fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ResourceLocation font = new ResourceLocation(jsonObject.get("font").getAsString());
        float lineHeight = jsonObject.get("lineHeight").getAsFloat();
        float letterSpacing = jsonObject.get("letterSpacing").getAsFloat();
        float lineSpacing = jsonObject.get("lineSpacing").getAsFloat();
        Alignment textAlignment = Alignment.fromJson(jsonObject.get("textAlignment"));
        int textColor = jsonObject.get("textColor").getAsInt();
        int bgColor = jsonObject.get("bgColor").getAsInt();
        boolean bold = jsonObject.get("bold").getAsBoolean();
        boolean italic = jsonObject.get("italic").getAsBoolean();
        boolean underline = jsonObject.get("underline").getAsBoolean();
        boolean strikethrough = jsonObject.get("strikethrough").getAsBoolean();
        boolean obfuscated = jsonObject.get("obfuscated").getAsBoolean();
        boolean withShadow = jsonObject.get("withShadow").getAsBoolean();

        IAnimation strikethroughAnimation = null, underlineAnimation = null, bgAnimation = null;
        if(jsonObject.has("strikethroughAnimation")) {
            strikethroughAnimation = IAnimation.fromJson(jsonObject.get("strikethroughAnimation"));
        }
        if(jsonObject.has("underlineAnimation")) {
            underlineAnimation = IAnimation.fromJson(jsonObject.get("underlineAnimation"));
        }
        if(jsonObject.has("bgAnimation")) {
            bgAnimation = IAnimation.fromJson(jsonObject.get("bgAnimation"));
        }

        return new FontFormatting(font, lineHeight, letterSpacing, lineSpacing, textAlignment, textColor, bgColor, bold, italic, underline, strikethrough, obfuscated, withShadow).setStrikethroughAnimation(strikethroughAnimation).setUnderlineAnimation(underlineAnimation).setBgAnimation(bgAnimation);
    }


    public FontFormatting setBgAnimation(@Nullable IAnimation bgAnimation) {
        this.bgAnimation = bgAnimation;
        return this;
    }


    public FontFormatting setUnderlineAnimation(@Nullable IAnimation underlineAnimation) {
        this.underlineAnimation = underlineAnimation;
        return this;
    }


    public ResourceLocation getFont() {
        return font;
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


    public FontFormatting setFont(ResourceLocation font) {
        this.font = font;
        return this;
    }


    public boolean isWithShadow() {
        return withShadow;
    }


    public @Nullable IAnimation getUnderlineAnimation() {
        return underlineAnimation;
    }


    public FontFormatting setWithShadow(boolean withShadow) {
        this.withShadow = withShadow;
        return this;
    }


    public @Nullable IAnimation getBgAnimation() {
        return bgAnimation;
    }


    public @Nullable IAnimation getStrikethroughAnimation() {
        return strikethroughAnimation;
    }

    public FontFormatting addTextEffect(ITextEffect effect) {
        this.textEffects.add(effect);
        return this;
    }


    public ImmutableSet<ITextEffect> getTextEffects() {
        return ImmutableSet.copyOf(this.textEffects);
    }


    public FontFormatting setStrikethroughAnimation(@Nullable IAnimation strikethroughAnimation) {
        this.strikethroughAnimation = strikethroughAnimation;
        return this;
    }


    public FontFormatting copy() {
        FontFormatting formatting = new FontFormatting(this.font, this.lineHeight, this.letterSpacing, this.lineSpacing, this.textAlignment, this.getColor(), this.getBgColor(), this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.withShadow);
        formatting.setGlyphTransform(this.glyphTransform);
        formatting.setBgAnimation(this.bgAnimation);
        formatting.setStrikethroughAnimation(this.strikethroughAnimation);
        formatting.setUnderlineAnimation(this.underlineAnimation);

        for(ITextEffect effect : this.textEffects) {
            formatting.addTextEffect(effect);
        }

        return formatting;
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("font", this.font.toString());
        jsonObject.addProperty("lineHeight", this.lineHeight);
        jsonObject.addProperty("letterSpacing", this.letterSpacing);
        jsonObject.addProperty("lineSpacing", this.lineSpacing);
        jsonObject.add("textAlignment", this.textAlignment.toJson());
        jsonObject.addProperty("textColor", this.getColor());
        jsonObject.addProperty("bgColor", this.getBgColor());
        jsonObject.addProperty("bold", this.bold);
        jsonObject.addProperty("italic", this.italic);
        jsonObject.addProperty("underlined", this.underlined);
        jsonObject.addProperty("strikethrough", this.strikethrough);
        jsonObject.addProperty("obfuscated", this.obfuscated);
        jsonObject.addProperty("withShadow", this.withShadow);
        if(this.strikethroughAnimation != null) {
            jsonObject.add("strikethroughAnimation", this.strikethroughAnimation.toJson());
        }
        if(this.underlineAnimation != null) {
            jsonObject.add("underlineAnimation", this.underlineAnimation.toJson());
        }
        if(this.bgAnimation != null) {
            jsonObject.add("bgAnimation", this.bgAnimation.toJson());
        }
        /*if(!this.textEffects.isEmpty()) {
            JsonArray jsonArray = new JsonArray(this.textEffects.size());
            for(ITextEffect textEffect : this.textEffects) jsonArray.add(textEffect.toJson());
        }*/

        return jsonObject;
    }


    public @Nullable IGlyphTransform getGlyphTransform() {
        return glyphTransform;
    }


    public FontFormatting setGlyphTransform(IGlyphTransform glyphTransform) {
        this.glyphTransform = glyphTransform;
        return this;
    }
}
