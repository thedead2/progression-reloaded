package de.thedead2.progression_reloaded.client.gui.fonts.rendering;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Transformation;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.TextCharIterator;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;


public class FontRenderer {
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);


    private static boolean isBidirectional() {
        return Language.getInstance().isDefaultRightToLeft();
    }


    public static int draw(PoseStack poseStack, FormattedCharSeq text, float pX, float pY, float pZ) {
        return drawInternal(text, pX, pY, pZ, poseStack.last().pose(), false, 15728880);
    }
    public static int draw(PoseStack poseStack, String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return draw(poseStack, text, pX, pY, pZ, font, formatting, false);
    }
    public static int draw(PoseStack poseStack, String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), false, transparent, isBidirectional(), 15728880);
    }
    public static int draw(PoseStack poseStack, FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return draw(poseStack, text, pX, pY, pZ, font, formatting, false);
    }
    public static int draw(PoseStack poseStack, FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), false, transparent, 15728880);
    }


    private static int drawInternal(FormattedCharSeq text, float pX, float pY, float pZ, Matrix4f matrix, boolean transparent, int packedLight) {
        if(text == null) {
            return 0;
        }

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        TextRenderer textRendererA = new TextRenderer(bufferSource, matrix, pX, pY, pZ, transparent, packedLight);
        Matrix4f matrix4f = new Matrix4f(matrix);
        matrix4f.translate(SHADOW_OFFSET);
        TextRenderer textRendererB = new TextRenderer(bufferSource, matrix4f, pX, pY, pZ, transparent, packedLight);

        TextCharIterator.iterate(text, (charPos, format, codePoint) -> {
            if(format.isWithShadow()) {
                textRendererA.visit(charPos, format, codePoint);
            }
            return textRendererB.visit(charPos, format.copy().setWithShadow(false), codePoint);
        });

        float xMax = textRendererA.getCharXPos();

        bufferSource.endBatch();
        if(ModRenderer.isGuiDebug()) {
            PoseStack poseStack = new PoseStack();
            poseStack.pushTransformation(new Transformation(matrix));
            RenderUtil.renderSquareOutlineDebug(poseStack, pX, xMax, pY, pY + text.getMaxCharHeight(), pZ, Color.YELLOW.getRGB());
        }
        return (int) xMax;
    }


    public static int draw(PoseStack poseStack, FormattedCharSeq text, float pX, float pY, float pZ, boolean transparent) {
        return drawInternal(text, pX, pY, pZ, poseStack.last().pose(), transparent, 15728880);
    }

    public static int draw(PoseStack poseStack, Component text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return draw(poseStack, text, pX, pY, pZ, font, formatting, false);
    }
    public static int draw(PoseStack poseStack, Component text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text.getVisualOrderText(), pX, pY, pZ, font, formatting, poseStack.last().pose(), false, transparent, 15728880);
    }
    public static int drawShadow(PoseStack poseStack, String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), true, false, isBidirectional(),15728880);
    }
    public static int drawShadow(PoseStack poseStack, String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), true, transparent, isBidirectional(), 15728880);
    }
    public static int drawShadow(PoseStack poseStack, FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), true, false, 15728880);
    }
    public static int drawShadow(PoseStack poseStack, FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text, pX, pY, pZ, font, formatting, poseStack.last().pose(), true, transparent, 15728880);
    }
    public static int drawShadow(PoseStack poseStack, Component text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting) {
        return drawInternal(text.getVisualOrderText(), pX, pY, pZ, font, formatting, poseStack.last().pose(), true, false, 15728880);
    }
    public static int drawShadow(PoseStack poseStack, Component text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, boolean transparent) {
        return drawInternal(text.getVisualOrderText(), pX, pY, pZ, font, formatting, poseStack.last().pose(), true, transparent, 15728880);
    }


    private static int drawInternal(String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, Matrix4f matrix, boolean withShadow, boolean transparent, boolean biDirectional, int packedLight) {
        if(text == null) return 0;

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        if (biDirectional) {
            text = bidirectionalShaping(text);
        }

        Matrix4f matrix4f = new Matrix4f(matrix);
        if (withShadow) {
            renderText(text, pX, pY, pZ, font, formatting, matrix, bufferSource, true, transparent, packedLight);
            matrix4f.translate(SHADOW_OFFSET);
        }

        float xMax = renderText(text, pX, pY, pZ, font, formatting, matrix4f, bufferSource, false, transparent, packedLight);
        bufferSource.endBatch();
        if(ModRenderer.isGuiDebug()){
            PoseStack poseStack = new PoseStack();
            poseStack.pushTransformation(new Transformation(matrix));
            RenderUtil.renderSquareOutlineDebug(poseStack, pX, xMax, pY, pY + formatting.getLineHeight() + formatting.getLineSpacing(), pZ, Color.YELLOW.getRGB());
        }
        return (int) xMax + (withShadow ? 1 : 0);
    }


    private static String bidirectionalShaping(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch(ArabicShapingException arabicshapingexception) {
            return text;
        }
    }


    private static float renderText(String text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, Matrix4f matrix, MultiBufferSource bufferSource, boolean withShadow, boolean transparent, int packedLight) {
        TextRenderer textRenderer = new TextRenderer(bufferSource, matrix, pX, pY, pZ, transparent, packedLight);
        formatting.setFont(font.getName()).setWithShadow(withShadow);
        TextCharIterator.iterate(text, formatting, textRenderer);
        return textRenderer.getCharXPos();
    }


    private static int drawInternal(FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, Matrix4f matrix, boolean withShadow, boolean transparent, int packedLight) {
        if(text == null) return 0;

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Matrix4f matrix4f = new Matrix4f(matrix);
        if (withShadow) {
            renderText(text, pX, pY, pZ, font, formatting, matrix, bufferSource, true, transparent, packedLight);
            matrix4f.translate(SHADOW_OFFSET);
        }

        float xMax = renderText(text, pX, pY, pZ, font, formatting, matrix4f, bufferSource, false, transparent, packedLight);
        bufferSource.endBatch();
        if(ModRenderer.isGuiDebug()){
            PoseStack poseStack = new PoseStack();
            poseStack.pushTransformation(new Transformation(matrix));
            RenderUtil.renderSquareOutlineDebug(poseStack, pX, xMax, pY, pY + formatting.getLineHeight() + formatting.getLineSpacing(), pZ, Color.YELLOW.getRGB());
        }
        return (int) xMax + (withShadow ? 1 : 0);
    }


    private static float renderText(FormattedCharSequence text, float pX, float pY, float pZ, @NotNull ProgressionFont font, @NotNull FontFormatting formatting, Matrix4f matrix, MultiBufferSource bufferSource, boolean withShadow, boolean transparent, int packedLight) {
        TextRenderer textRenderer = new TextRenderer(bufferSource, matrix, pX, pY, pZ, transparent, packedLight);
        formatting.setFont(font.getName()).setWithShadow(withShadow);
        text.accept((pos, style, codePoint) -> textRenderer.visit(pos, formatting, codePoint));
        return textRenderer.getCharXPos();
    }
}
