package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.mojang.blaze3d.vertex.VertexConsumer;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.api.gui.fonts.ITextEffect;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;


public class TextEffects {
    public static ITextEffect STRIKETHROUGH(float xMin, float xMax, float yPos, float zPos, float scaling, float red, float green, float blue, float alpha) {
        return LINE(xMin, xMax, yPos, zPos, 2 * scaling, red, green, blue, alpha);
    }
    public static ITextEffect UNDERLINED(float xMin, float xMax, float yPos, float zPos, float scaling, float red, float green, float blue, float alpha) {
        return LINE(xMin, xMax, yPos, zPos, 2 * scaling, red, green, blue, alpha);
    }


    public static ITextEffect LINE(float xMin, float xMax, float yPos, float zPos, float height, float red, float green, float blue, float alpha) {
        float yMin = yPos - height / 2;
        float yMax = yPos + height / 2;
        return (whiteGlyph, scaling, matrix, buffer, packedLight) -> {
            buffer.vertex(matrix, xMin, yMin, zPos).color(red, green, blue, alpha).uv(whiteGlyph.getU0(), whiteGlyph.getV0()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, xMax, yMin, zPos).color(red, green, blue, alpha).uv(whiteGlyph.getU0(), whiteGlyph.getV1()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, xMax, yMax, zPos).color(red, green, blue, alpha).uv(whiteGlyph.getU1(), whiteGlyph.getV1()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, xMin, yMax, zPos).color(red, green, blue, alpha).uv(whiteGlyph.getU1(), whiteGlyph.getV0()).uv2(packedLight).endVertex();
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static class LineEffect implements ITextEffect {

        @Nullable
        private final IAnimation animation;
        protected final float x0;

        protected final float y0;

        protected final float x1;

        protected final float y1;

        protected final float depth;

        protected final float red;

        protected final float green;

        protected final float blue;

        protected final float alpha;


        public LineEffect(@Nullable IAnimation animation, float pX0, float pY0, float pX1, float pY1, float pDepth, float pR, float pG, float pB, float pA) {
            this.animation = animation;
            this.x0 = pX0;
            this.y0 = pY0;
            this.x1 = pX1;
            this.y1 = pY1;
            this.depth = pDepth;
            this.red = pR;
            this.green = pG;
            this.blue = pB;
            this.alpha = pA;
        }


        @Override
        public void render(BakedFontGlyph whiteGlyph, float scaling, Matrix4f matrix, VertexConsumer buffer, int packedLight) {
            AtomicReference<Float> x1 = new AtomicReference<>(this.x1);
            if(this.animation != null) this.animation.animate(this.x0, this.x1, x1::set);

            buffer.vertex(matrix, this.x0, this.y0, this.depth).color(this.red, this.green, this.blue, this.alpha).uv(whiteGlyph.getU0(), whiteGlyph.getV0()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, x1.get(), this.y0, this.depth).color(this.red, this.green, this.blue, this.alpha).uv(whiteGlyph.getU0(), whiteGlyph.getV1()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, x1.get(), this.y1, this.depth).color(this.red, this.green, this.blue, this.alpha).uv(whiteGlyph.getU1(), whiteGlyph.getV1()).uv2(packedLight).endVertex();
            buffer.vertex(matrix, this.x0, this.y1, this.depth).color(this.red, this.green, this.blue, this.alpha).uv(whiteGlyph.getU1(), whiteGlyph.getV0()).uv2(packedLight).endVertex();
        }
    }
}
