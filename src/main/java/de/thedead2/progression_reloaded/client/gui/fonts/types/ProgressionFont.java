package de.thedead2.progression_reloaded.client.gui.fonts.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedText;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.TextWrapper;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.SpecialGlyphs;
import de.thedead2.progression_reloaded.client.gui.fonts.providers.SpaceProvider;
import de.thedead2.progression_reloaded.client.gui.fonts.rendering.FontRenderer;
import de.thedead2.progression_reloaded.client.gui.textures.FontTexture;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


/**
 * Complete rework of minecrafts font system to support different line heights, line spacing, letter spacing,
 * easier color transformation, dynamic animations any many more
 * **/

public class ProgressionFont {
    private static final RandomSource RANDOM = RandomSource.create();
    private final ResourceLocation name;

    private FontFormatting formatting = FontFormatting.defaultFormatting();
    private BakedFontGlyph missingGlyph;
    private BakedFontGlyph whiteGlyph;
    private final List<IFontGlyphProvider> glyphProviders = Lists.newArrayList();
    private final Int2ObjectMap<IUnbakedGlyph> unbakedGlyphs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<BakedFontGlyph> bakedGlyphs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
    private final List<FontTexture> textures = Lists.newArrayList();
    private final Int2ObjectMap<Float2FloatFunction> scalingFactors = new Int2ObjectOpenHashMap<>();
    private final TextWrapper textWrapper;
    private Boolean hasSpaceProvider;


    public ProgressionFont(ResourceLocation name, List<IFontGlyphProvider> glyphProviders) {
        this.name = name;
        this.textWrapper = new TextWrapper((codePoint, formatting) -> {
            float scalingFactor = this.getScalingFactor(codePoint, formatting.getLineHeight());
            IUnbakedGlyph glyph = this.getUnbakedGlyph(codePoint);
            return (glyph.getAdvance(formatting.isBold()) * scalingFactor) + formatting.getLetterSpacing();
        });
        this.load(glyphProviders);
    }

    public ResourceLocation getName() {
        return name;
    }

    public void load(Collection<IFontGlyphProvider> glyphProviders) {
        this.close();
        this.bakedGlyphs.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
        this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
        this.hasSpaceProvider = null;
        IntSet intset = new IntOpenHashSet();

        for(IFontGlyphProvider glyphProvider : glyphProviders) {
            intset.addAll(glyphProvider.getSupportedGlyphs());
        }

        Set<IFontGlyphProvider> set = Sets.newHashSet();
        intset.forEach((int i) -> {
            for(IFontGlyphProvider glyphProvider : glyphProviders) {
                IUnbakedGlyph unbakedGlyph = glyphProvider.getUnbakedGlyph(i);
                if (unbakedGlyph != null) {
                    set.add(glyphProvider);
                    if(hasSpaceProvider == null && glyphProvider instanceof SpaceProvider) {
                        hasSpaceProvider = true;
                    }

                    if (unbakedGlyph != SpecialGlyphs.MISSING) {
                        this.glyphsByWidth.computeIfAbsent(Mth.ceil(unbakedGlyph.getAdvance(false)), (i1) -> new IntArrayList()).add(i);
                        this.scalingFactors.putIfAbsent(i, glyphProvider::getScalingFactor);
                    }
                    break;
                }
            }
        });
        if(hasSpaceProvider == null) hasSpaceProvider = false;
        glyphProviders.stream().filter(set::contains).forEach(this.glyphProviders::add);
    }

    public void close() {
        this.closeTextures();
        this.closeProviders();
    }

    private void closeProviders() {
        for(IFontGlyphProvider glyphProvider : this.glyphProviders) {
            glyphProvider.close();
        }

        this.glyphProviders.clear();
    }

    private void closeTextures() {
        for(FontTexture fonttexture : this.textures) {
            fonttexture.close();
        }

        this.textures.clear();
    }

    private BakedFontGlyph computeBakedGlyph(int character) {
        IUnbakedGlyph unbakedGlyph = this.getUnbakedGlyph(character);
        return unbakedGlyph.bake(this::stitch);

    }

    public BakedFontGlyph getGlyph(int character) {
        return this.bakedGlyphs.computeIfAbsent(character, this::computeBakedGlyph);
    }

    private BakedFontGlyph stitch(IGlyphInfo iGlyphInfo) {
        for(FontTexture fonttexture : this.textures) {
            BakedFontGlyph bakedFontGlyph = fonttexture.add(iGlyphInfo);
            if (bakedFontGlyph != null) {
                return bakedFontGlyph;
            }
        }

        FontTexture fontTexture = new FontTexture(this.name.withPath((s) -> "pr/" + s + "/" + this.textures.size()), iGlyphInfo.isColored());
        this.textures.add(fontTexture);
        Minecraft.getInstance().textureManager.register(fontTexture.getName(), fontTexture);
        BakedFontGlyph bakedFontGlyph = fontTexture.add(iGlyphInfo);
        return bakedFontGlyph == null ? this.missingGlyph : bakedFontGlyph;
    }

    public BakedFontGlyph getRandomGlyph(IUnbakedGlyph glyph) {
        IntList intlist = this.glyphsByWidth.get(Mth.ceil(glyph.getAdvance(false)));
        return intlist != null && !intlist.isEmpty() ? this.getGlyph(intlist.getInt(RANDOM.nextInt(intlist.size()))) : this.missingGlyph;
    }

    public float width(String text) {
        return this.textWrapper.stringWidth(text, this.formatting);
    }

    public float width(net.minecraft.network.chat.FormattedText text) {
        return this.textWrapper.stringWidth(text, this.formatting);
    }

    public float width(FormattedCharSequence text) {
        return this.textWrapper.stringWidth(text, this.formatting);
    }

    public float height(String text, float maxWidth) {
        return (this.getLineHeight() + this.formatting.getLineSpacing()) * this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth).size();
    }
    public float height(net.minecraft.network.chat.FormattedText text, float maxWidth) {
        return (this.getLineHeight() + this.formatting.getLineSpacing()) * this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth).size();
    }
    public float height(FormattedCharSequence text, float maxWidth) {
        return (this.getLineHeight() + this.formatting.getLineSpacing()) * this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth).size();
    }

    public float getScalingFactor(int codePoint, float px) {
        float factor = this.scalingFactors.getOrDefault(codePoint, i -> 1).get(px);
        return factor == 0 ? this.scalingFactors.getOrDefault('a', i -> 1).get(px) : factor;
    }

    @NotNull
    public IUnbakedGlyph getUnbakedGlyph(int character) {
        return this.unbakedGlyphs.computeIfAbsent(character, this::computeUnbakedGlyph);
    }


    private IUnbakedGlyph computeUnbakedGlyph(int character) {
        IUnbakedGlyph unbakedGlyph = null;

        for(IFontGlyphProvider glyphProvider : this.glyphProviders) {
            IUnbakedGlyph unbakedGlyph1 = glyphProvider.getUnbakedGlyph(character);
            if (unbakedGlyph1 != null && !checkForSpaceFromSpaceProvider(character, glyphProvider)) {
                unbakedGlyph = unbakedGlyph1;
                break;
            }
        }

        return unbakedGlyph != null ? unbakedGlyph : SpecialGlyphs.MISSING;
    }

    private boolean checkForSpaceFromSpaceProvider(int character, IFontGlyphProvider glyphProvider) {
        return character == 32 && this.hasSpaceProvider && !(glyphProvider instanceof SpaceProvider);
    }


    public BakedFontGlyph getWhiteGlyph() {
        return whiteGlyph;
    }

    public ProgressionFont format(FontFormatting formatting) {
        this.formatting = formatting;
        return this;
    }

    public ProgressionFont defaultFormatting() {
        this.formatting = FontFormatting.defaultFormatting();
        return this;
    }

    public ProgressionFont setLineHeight(float lineHeight) {
        this.formatting.setLineHeight(lineHeight);
        return this;
    }


    public ProgressionFont setLetterSpacing(float letterSpacing) {
        this.formatting.setLetterSpacing(letterSpacing);
        return this;
    }


    public ProgressionFont setLineSpacing(float lineSpacing) {
        this.formatting.setLineSpacing(lineSpacing);
        return this;
    }


    public ProgressionFont setTextAlignment(Alignment textAlignment) {
        this.formatting.setTextAlignment(textAlignment);
        return this;
    }


    public ProgressionFont setColor(int color) {
        this.formatting.setColor(color);
        return this;
    }


    public ProgressionFont setBgColor(int bgColor) {
        this.formatting.setBgColor(bgColor);
        return this;
    }


    public ProgressionFont setBold(boolean bold) {
        this.formatting.setBold(bold);
        return this;
    }


    public ProgressionFont setItalic(boolean italic) {
        this.formatting.setItalic(italic);
        return this;
    }


    public ProgressionFont setUnderlined(boolean underlined) {
        this.formatting.setUnderlined(underlined);
        return this;
    }


    public ProgressionFont setStrikethrough(boolean strikethrough) {
        this.formatting.setStrikethrough(strikethrough);
        return this;
    }

    public float getLineHeight() {
        return this.formatting.getLineHeight();
    }


    public FontFormatting getFormatting() {
        return formatting;
    }


    public float draw(PoseStack poseStack, String text, float x, float y, float z) {
        return FontRenderer.draw(poseStack, text, x, y, z, this, this.formatting);
    }
    public float draw(PoseStack poseStack, Component text, float x, float y, float z) {
        return FontRenderer.draw(poseStack, text, x, y, z, this, this.formatting);
    }
    public float draw(PoseStack poseStack, FormattedCharSequence text, float x, float y, float z) {
        return FontRenderer.draw(poseStack, text, x, y, z, this, this.formatting);
    }

    public float drawShadow(PoseStack poseStack, String text, float x, float y, float z) {
        return FontRenderer.drawShadow(poseStack, text, x, y, z, this, this.formatting);
    }
    public float drawShadow(PoseStack poseStack, Component text, float x, float y, float z) {
        return FontRenderer.drawShadow(poseStack, text, x, y, z, this, this.formatting);
    }
    public float drawShadow(PoseStack poseStack, FormattedCharSequence text, float x, float y, float z) {
        return FontRenderer.drawShadow(poseStack, text, x, y, z, this, this.formatting);
    }

    public float drawWithLineWrap(PoseStack poseStack, String text, Function<FormattedText, Float> x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth)) {
            this.draw(poseStack, formattedText.text(), x.apply(formattedText), i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }
        return i;
    }
    public float drawWithLineWrap(PoseStack poseStack, Component text, float x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth)) {
            this.draw(poseStack, formattedText.text(), x, i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }
        return i;
    }
    public float drawWithLineWrap(PoseStack poseStack, FormattedCharSequence text, float x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, false, maxWidth)) {
            this.draw(poseStack, formattedText.text(), x, i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }
        return i;
    }

    public float drawShadowWithLineWrap(PoseStack poseStack, String text, Function<FormattedText, Float> x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, true, maxWidth)) {
            this.drawShadow(poseStack, formattedText.text(), x.apply(formattedText), i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }

        return i;
    }
    public float drawShadowWithLineWrap(PoseStack poseStack, Component text, float x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, true, maxWidth)) {
            this.drawShadow(poseStack, formattedText.text(), x, i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }

        return i;
    }
    public float drawShadowWithLineWrap(PoseStack poseStack, FormattedCharSequence text, float x, float y, float z, float maxWidth) {
        float i = y;
        for(FormattedText formattedText : this.textWrapper.splitLines(text, this.name, this.formatting, true, maxWidth)) {
            this.drawShadow(poseStack, formattedText.text(), x, i, z);
            i += this.formatting.getLineHeight() + this.formatting.getLineSpacing();
        }

        return i;
    }


    public TextWrapper getTextWrapper() {
        return textWrapper;
    }

    public ProgressionFont setStrikeThroughAnimation(@Nullable IAnimation strikeThroughAnimation) {
        this.formatting.setStrikeThroughAnimation(strikeThroughAnimation);
        return this;
    }


    public ProgressionFont setUnderlineAnimation(@Nullable IAnimation underlineAnimation) {
        this.formatting.setUnderlineAnimation(underlineAnimation);
        return this;
    }


    public ProgressionFont setBgAnimation(@Nullable IAnimation bgAnimation) {
        this.formatting.setBgAnimation(bgAnimation);
        return this;
    }

    public ProgressionFont addTextEffect(ITextEffect effect) {
        this.formatting.addTextEffect(effect);
        return this;
    }


    public ProgressionFont setAlpha(float alpha) {
        this.formatting.setAlpha(Math.round(alpha * 255));
        return this;
    }
    public ProgressionFont setRed(float red) {
        this.formatting.setRed(Math.round(red * 255));
        return this;
    }
    public ProgressionFont setGreen(float green) {
        this.formatting.setGreen(Math.round(green * 255));
        return this;
    }
    public ProgressionFont setBlue(float blue) {
        this.formatting.setBlue(Math.round(blue * 255));
        return this;
    }
    public ProgressionFont setAlpha(int alpha) {
        this.formatting.setAlpha(alpha);
        return this;
    }
    public ProgressionFont setRed(int red) {
        this.formatting.setRed(red);
        return this;
    }
    public ProgressionFont setGreen(int green) {
        this.formatting.setGreen(green);
        return this;
    }
    public ProgressionFont setBlue(int blue) {
        this.formatting.setBlue(blue);
        return this;
    }
    public ProgressionFont setBgAlpha(float alpha) {
        this.formatting.setBgAlpha(Math.round(alpha * 255));
        return this;
    }
    public ProgressionFont setBgRed(float red) {
        this.formatting.setBgRed(Math.round(red * 255));
        return this;
    }
    public ProgressionFont setBgGreen(float green) {
        this.formatting.setBgGreen(Math.round(green * 255));
        return this;
    }
    public ProgressionFont setBgBlue(float blue) {
        this.formatting.setBgBlue(Math.round(blue * 255));
        return this;
    }
    public ProgressionFont setBgAlpha(int alpha) {
        this.formatting.setBgAlpha(alpha);
        return this;
    }
    public ProgressionFont setBgRed(int red) {
        this.formatting.setBgRed(red);
        return this;
    }
    public ProgressionFont setBgGreen(int green) {
        this.formatting.setBgGreen(green);
        return this;
    }
    public ProgressionFont setBgBlue(int blue) {
        this.formatting.setBgBlue(blue);
        return this;
    }
}
