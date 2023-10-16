package de.thedead2.progression_reloaded.client.gui.fonts.rendering;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.*;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.List;


public class TextRenderer implements TextCharIterator.ICharVisitor {
    @NotNull
    private final ProgressionFont font;
    @NotNull
    private final FontFormatting formatting;
    private final MultiBufferSource bufferSource;
    private final Matrix4f matrix;
    private final Font.DisplayMode displayMode;
    private final float startXPos;
    private final float yPos;
    private final float zPos;
    private float charXPos;
    private final boolean withShadow;
    private final int packedLight;
    @Nullable
    private List<ITextEffect> textEffects;


    public TextRenderer(@NotNull ProgressionFont font, @NotNull FontFormatting formatting, MultiBufferSource bufferSource, Matrix4f matrix, float xPos, float yPos, float zPos, boolean withShadow, boolean transparent, int packedLight) {
        this(font, formatting, bufferSource, matrix, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, xPos, yPos, zPos, withShadow, packedLight);
    }
    public TextRenderer(@NotNull ProgressionFont font, @NotNull FontFormatting formatting, MultiBufferSource bufferSource, Matrix4f matrix, Font.DisplayMode displayMode, float xPos, float yPos, float zPos, boolean withShadow, int packedLight) {
        this.font = font;
        this.formatting = formatting;
        this.bufferSource = bufferSource;
        this.matrix = matrix;
        this.displayMode = displayMode;
        this.startXPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.charXPos = xPos;
        this.withShadow = withShadow;
        this.packedLight = packedLight;
    }


    private void addEffect(ITextEffect effect) {
        if(this.textEffects == null) {
            this.textEffects = Lists.newArrayList();
        }

        if(!this.textEffects.contains(effect))
            this.textEffects.add(effect);
    }

    @Override
    public boolean visit(int charPos, int codePoint) {
        IUnbakedGlyph unbakedGlyph = font.getUnbakedGlyph(codePoint);
        BakedFontGlyph bakedFontGlyph = formatting.isObfuscated() && codePoint != 32 ? font.getRandomGlyph(unbakedGlyph) : font.getGlyph(codePoint);
        boolean bold = formatting.isBold();
        float dimFactor = this.withShadow ? 0.25F : 1.0F;
        float alpha = (float) this.formatting.getAlpha() / 255.0F * dimFactor;
        float red = (float) this.formatting.getRed() / 255.0F * dimFactor;
        float green = (float) this.formatting.getGreen() / 255.0F * dimFactor;
        float blue = (float) this.formatting.getBlue() / 255.0F * dimFactor;

        float scalingFactor = this.font.getScalingFactor(codePoint, this.formatting.getLineHeight());

        if(!(bakedFontGlyph instanceof BakedFontGlyph.EmptyFontGlyph)) {
            float boldOffset = bold ? unbakedGlyph.getBoldOffset() : 0.0F;
            float shadowOffset = this.withShadow ? unbakedGlyph.getShadowOffset() : 0.0F;
            VertexConsumer buffer = this.bufferSource.getBuffer(bakedFontGlyph.renderType(this.displayMode));
            renderChar(bakedFontGlyph, this.charXPos + shadowOffset, this.yPos + shadowOffset, this.zPos, scalingFactor, this.matrix, buffer, bold, formatting.isItalic(), boldOffset, red, green, blue, alpha, this.packedLight);
        }

        float charGap = unbakedGlyph.getAdvance(bold) * scalingFactor + formatting.getLetterSpacing();

        this.charXPos += charGap;
        return true;
    }

    //TODO: Animations for individual chars?!
    public static void renderChar(BakedFontGlyph glyph, float x, float y, float z, float scaling, Matrix4f matrix, VertexConsumer buffer, boolean bold, boolean italic, float boldOffset, float red, float green, float blue, float alpha, int packedLight) {

        glyph.render(x, y, z, scaling, matrix, buffer, italic, red, green, blue, alpha, packedLight);
        if (bold) {
            glyph.render(x + boldOffset, y, z, scaling, matrix, buffer, italic, red, green, blue, alpha, packedLight);
        }
    }


    public float finish() {
        int bgColor = this.formatting.getBgColor();
        if(bgColor != 0) {
            float alpha = (float) this.formatting.getBgAlpha() / 255.0F;
            float red = (float) this.formatting.getBgRed() / 255.0F;
            float green = (float) this.formatting.getBgGreen() / 255.0F;
            float blue = (float) this.formatting.getBgBlue() / 255.0F;

            this.addEffect(new TextEffects.LineEffect(this.formatting.getBgAnimation(), this.startXPos - 1.0F, this.yPos + this.formatting.getLineHeight(), this.charXPos + 1.0F, this.yPos - 1.0F, this.zPos - 1, red, green, blue, alpha));
        }

        float dimFactor = this.withShadow ? 0.25F : 1.0F;
        float alpha = (float) this.formatting.getAlpha() / 255.0F * dimFactor;
        float red = (float) this.formatting.getRed() / 255.0F * dimFactor;
        float green = (float) this.formatting.getGreen() / 255.0F * dimFactor;
        float blue = (float) this.formatting.getBlue() / 255.0F * dimFactor;

        float f7 = this.withShadow ? 1.0F : 0.0F;
        float lineHeight = (1 * font.getScalingFactor('a', this.formatting.getLineHeight())) / 2;
        if(formatting.isStrikethrough()) {
            this.addEffect(new TextEffects.LineEffect(this.formatting.getStrikeThroughAnimation(), this.startXPos - 1.0F, this.yPos + f7 + this.formatting.getLineHeight() / 2 + lineHeight, this.charXPos + f7, this.yPos + f7 + this.formatting.getLineHeight() / 2 - lineHeight, this.zPos + 1, red, green, blue, alpha));
        }
        if(formatting.isUnderlined()) {
            this.addEffect(new TextEffects.LineEffect(this.formatting.getUnderlineAnimation(), this.startXPos - 1.0F, this.yPos + f7 + this.formatting.getLineHeight() + lineHeight, this.charXPos + f7, this.yPos + f7 + this.formatting.getLineHeight() - lineHeight, this.zPos + 1, red, green, blue, alpha));
        }

        for (ITextEffect effect : this.formatting.getTextEffects()) {
            this.addEffect(effect);
        }

        if(this.textEffects != null) {
            BakedFontGlyph whiteGlyph = this.font.getWhiteGlyph();
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(whiteGlyph.renderType(this.displayMode));

            float scalingFactor = this.font.getScalingFactor('a', this.formatting.getLineHeight());

            for(ITextEffect effect : this.textEffects) {
                effect.render(whiteGlyph, scalingFactor, this.matrix, vertexconsumer, this.packedLight);
            }
        }

        return this.charXPos;
    }
}
