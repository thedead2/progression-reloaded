package de.thedead2.progression_reloaded.client.gui.fonts.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;


public class BakedFontGlyph {
    private final RenderType normalType;
    private final RenderType seeThroughType;
    private final RenderType polygonOffsetType;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;
    public BakedFontGlyph(RenderType pNormalType, RenderType pSeeThroughType, RenderType pPolygonOffsetType, float pU0, float pU1, float pV0, float pV1, float left, float right, float up, float down) {
        this.normalType = pNormalType;
        this.seeThroughType = pSeeThroughType;
        this.polygonOffsetType = pPolygonOffsetType;
        this.u0 = pU0;
        this.u1 = pU1;
        this.v0 = pV0;
        this.v1 = pV1;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }

    public void render(float x, float y, float z, float scaling, Matrix4f matrix, VertexConsumer buffer, boolean italic, float red, float green, float blue, float alpha, int packedLight) {
        int margin = 3;
        float xLeftWidth = x + this.left * scaling;
        float xRightWidth = x + this.right * scaling;
        float heightUp = this.up - 3.0F;
        float heightDown = this.down - 3.0F;
        float yHeightUp = y + heightUp * scaling;
        float yHeightDown = y + heightDown * scaling;
        float f6 = italic ? 1.0F - 0.25F * heightUp : 0.0F;
        float f7 = italic ? 1.0F - 0.25F * heightDown : 0.0F;
        buffer.vertex(matrix, xLeftWidth + f6, yHeightUp, z).color(red, green, blue, alpha).uv(this.u0, this.v0).uv2(packedLight).endVertex();
        buffer.vertex(matrix, xLeftWidth + f7, yHeightDown, z).color(red, green, blue, alpha).uv(this.u0, this.v1).uv2(packedLight).endVertex();
        buffer.vertex(matrix, xRightWidth + f7, yHeightDown, z).color(red, green, blue, alpha).uv(this.u1, this.v1).uv2(packedLight).endVertex();
        buffer.vertex(matrix, xRightWidth + f6, yHeightUp, z).color(red, green, blue, alpha).uv(this.u1, this.v0).uv2(packedLight).endVertex();
    }

    public RenderType renderType(Font.DisplayMode pDisplayMode) {
        return switch(pDisplayMode) {
            case NORMAL -> this.normalType;
            case SEE_THROUGH -> this.seeThroughType;
            case POLYGON_OFFSET -> this.polygonOffsetType;
        };
    }

    public float getWidth(float scale) {
        return this.left * scale + this.right * scale;
    }
    public float getHeight(float scale) {
        return (this.up - 3.0F) * scale + (this.down - 3.0F) * scale;
    }


    public float getU0() {
        return u0;
    }


    public float getU1() {
        return u1;
    }


    public float getV0() {
        return v0;
    }


    public float getV1() {
        return v1;
    }


    public static class EmptyFontGlyph extends BakedFontGlyph {
        public static final EmptyFontGlyph INSTANCE = new EmptyFontGlyph();

        public EmptyFontGlyph() {
            super(RenderType.text(new ResourceLocation("")), RenderType.textSeeThrough(new ResourceLocation("")), RenderType.textPolygonOffset(new ResourceLocation("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        }


        @Override
        public void render(float x, float y, float z, float scaling, Matrix4f matrix, VertexConsumer buffer, boolean italic, float red, float green, float blue, float alpha, int packedLight) {
        }
    }
}
