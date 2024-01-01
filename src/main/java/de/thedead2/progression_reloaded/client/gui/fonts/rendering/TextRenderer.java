package de.thedead2.progression_reloaded.client.gui.fonts.rendering;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphTransform;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.TextCharIterator;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.TextEffects;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.List;


public class TextRenderer implements TextCharIterator.ICharVisitor {
    private final MultiBufferSource bufferSource;
    private final Matrix4f matrix;
    private final Font.DisplayMode displayMode;
    private final float startXPos;
    private final float yPos;
    private final float zPos;
    private float charXPos;
    private final int packedLight;
    @Nullable
    private List<ITextEffect> textEffects;


    public TextRenderer(MultiBufferSource bufferSource, Matrix4f matrix, float xPos, float yPos, float zPos, boolean transparent, int packedLight) {
        this(bufferSource, matrix, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, xPos, yPos, zPos, packedLight);
    }


    public TextRenderer(MultiBufferSource bufferSource, Matrix4f matrix, Font.DisplayMode displayMode, float xPos, float yPos, float zPos, int packedLight) {
        this.bufferSource = bufferSource;
        this.matrix = matrix;
        this.displayMode = displayMode;
        this.startXPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.charXPos = xPos;
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
    public boolean visit(int charPos, FontFormatting formatting, int codePoint) {
        if(codePoint == '\n' && !ModRenderer.isGuiDebug()) {
            return true;
        }
        ProgressionFont font = FontManager.getInstance().getFont(formatting.getFont());
        IUnbakedGlyph unbakedGlyph = font.getUnbakedGlyph(codePoint);
        BakedFontGlyph bakedFontGlyph = formatting.isObfuscated() && codePoint != 32 ? font.getRandomGlyph(unbakedGlyph) : font.getGlyph(codePoint);
        boolean bold = formatting.isBold();
        float dimFactor = formatting.isWithShadow() ? 0.25F : 1.0F;
        float alpha = (float) formatting.getAlpha() / 255.0F * dimFactor;
        float red = (float) formatting.getRed() / 255.0F * dimFactor;
        float green = (float) formatting.getGreen() / 255.0F * dimFactor;
        float blue = (float) formatting.getBlue() / 255.0F * dimFactor;

        float scalingFactor = font.getScalingFactor(codePoint, formatting.getLineHeight());
        float charWidth = unbakedGlyph.getAdvance(bold) * scalingFactor + formatting.getLetterSpacing();

        IGlyphTransform glyphTransform = formatting.getGlyphTransform();
        Matrix4f matrix4f = new Matrix4f(this.matrix);
        if(glyphTransform != null) {
            glyphTransform.transform(bakedFontGlyph, this.matrix, this.charXPos, this.yPos, this.zPos, charWidth, formatting.getLineHeight());
        }

        if(!(bakedFontGlyph instanceof BakedFontGlyph.EmptyFontGlyph)) {
            float boldOffset = bold ? unbakedGlyph.getBoldOffset() : 0.0F;
            float shadowOffset = formatting.isWithShadow() ? unbakedGlyph.getShadowOffset() : 0.0F;
            VertexConsumer buffer = this.bufferSource.getBuffer(bakedFontGlyph.renderType(this.displayMode));
            renderChar(bakedFontGlyph, this.charXPos + shadowOffset, this.yPos + shadowOffset, this.zPos, scalingFactor, this.matrix, buffer, bold, formatting.isItalic(), boldOffset, red, green, blue, alpha, this.packedLight);
        }

        this.renderCharEffects(font, formatting);

        this.matrix.set(matrix4f);
        this.charXPos += charWidth;
        return true;
    }

    //TODO: Animations for individual chars?!
    public static void renderChar(BakedFontGlyph glyph, float x, float y, float z, float scaling, Matrix4f matrix, VertexConsumer buffer, boolean bold, boolean italic, float boldOffset, float red, float green, float blue, float alpha, int packedLight) {
        glyph.render(x, y, z, scaling, matrix, buffer, italic, red, green, blue, alpha, packedLight);
        if (bold) {
            glyph.render(x + boldOffset, y, z, scaling, matrix, buffer, italic, red, green, blue, alpha, packedLight);
        }
    }


    public void renderCharEffects(ProgressionFont font, FontFormatting formatting) {
        int bgColor = formatting.getBgColor();
        if(bgColor != 0) {
            float alpha = (float) formatting.getBgAlpha() / 255.0F;
            float red = (float) formatting.getBgRed() / 255.0F;
            float green = (float) formatting.getBgGreen() / 255.0F;
            float blue = (float) formatting.getBgBlue() / 255.0F;

            this.addEffect(new TextEffects.LineEffect(formatting.getBgAnimation(), this.startXPos - 1.0F, this.yPos + formatting.getLineHeight(), this.charXPos + 1.0F, this.yPos - 1.0F, this.zPos - 1, red, green, blue, alpha));
        }

        float dimFactor = formatting.isWithShadow() ? 0.25F : 1.0F;
        float alpha = (float) formatting.getAlpha() / 255.0F * dimFactor;
        float red = (float) formatting.getRed() / 255.0F * dimFactor;
        float green = (float) formatting.getGreen() / 255.0F * dimFactor;
        float blue = (float) formatting.getBlue() / 255.0F * dimFactor;

        float f7 = formatting.isWithShadow() ? 1.0F : 0.0F;
        float lineHeight = (1 * font.getScalingFactor('a', formatting.getLineHeight())) / 2;
        if(formatting.isStrikethrough()) {
            this.addEffect(new TextEffects.LineEffect(formatting.getStrikethroughAnimation(), this.startXPos - 1.0F, this.yPos + f7 + formatting.getLineHeight() / 2 + lineHeight, this.charXPos + f7, this.yPos + f7 + formatting.getLineHeight() / 2 - lineHeight, this.zPos + 1, red, green, blue, alpha));
        }
        if(formatting.isUnderlined()) {
            this.addEffect(new TextEffects.LineEffect(formatting.getUnderlineAnimation(), this.startXPos - 1.0F, this.yPos + f7 + formatting.getLineHeight() + lineHeight, this.charXPos + f7, this.yPos + f7 + formatting.getLineHeight() - lineHeight, this.zPos + 1, red, green, blue, alpha));
        }

        for(ITextEffect effect : formatting.getTextEffects()) {
            this.addEffect(effect);
        }

        if(this.textEffects != null) {
            BakedFontGlyph whiteGlyph = font.getWhiteGlyph();
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(whiteGlyph.renderType(this.displayMode));

            float scalingFactor = font.getScalingFactor('a', formatting.getLineHeight());

            for(ITextEffect effect : this.textEffects) {
                effect.render(whiteGlyph, scalingFactor, this.matrix, vertexconsumer, this.packedLight);
            }
        }
    }


    public float getCharXPos() {
        return this.charXPos;
    }
}
