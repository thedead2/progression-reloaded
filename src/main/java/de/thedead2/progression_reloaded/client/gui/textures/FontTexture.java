package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import javax.annotation.Nullable;

import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class FontTexture extends AbstractTexture {
    private static final int SIZE = 256;
    private final ResourceLocation name;
    private final RenderType normalType;
    private final RenderType seeThroughType;
    private final RenderType polygonOffsetType;
    private final boolean colored;
    private final FontTexture.Node root;

    public FontTexture(ResourceLocation name, boolean colored) {
        this.name = name;
        this.colored = colored;
        this.root = new FontTexture.Node(0, 0, 256, 256);
        TextureUtil.prepareImage(colored ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
        this.normalType = colored ? RenderType.text(name) : RenderType.textIntensity(name);
        this.seeThroughType = colored ? RenderType.textSeeThrough(name) : RenderType.textIntensitySeeThrough(name);
        this.polygonOffsetType = colored ? RenderType.textPolygonOffset(name) : RenderType.textIntensityPolygonOffset(name);
    }

    public void load(@NotNull ResourceManager manager) {}

    public void close() {
        this.releaseId();
    }

    @Nullable
    public BakedFontGlyph add(IGlyphInfo glyphInfo) {
        if (glyphInfo.isColored() != this.colored) {
            return null;
        } else {
            FontTexture.Node node = this.root.insert(glyphInfo);
            if (node != null) {
                this.bind();
                glyphInfo.upload(node.x, node.y);
                float f = 256.0F;
                float f1 = 256.0F;
                float margin = 0.01F;
                return new BakedFontGlyph(this.normalType, this.seeThroughType, this.polygonOffsetType, ((float)node.x + margin) / 256.0F, ((float)node.x - margin + (float)glyphInfo.getPixelWidth()) / 256.0F,
                                          ((float)node.y + margin) / 256.0F, ((float)node.y - margin + (float)glyphInfo.getPixelHeight()) / 256.0F, glyphInfo.getLeft(), glyphInfo.getRight(), glyphInfo.getUp(), glyphInfo.getDown());
            } else {
                return null;
            }
        }
    }

    public ResourceLocation getName() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    static class Node {
        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private FontTexture.Node left;
        @Nullable
        private FontTexture.Node right;
        private boolean occupied;

        Node(int pX, int pY, int pWidth, int pHeight) {
            this.x = pX;
            this.y = pY;
            this.width = pWidth;
            this.height = pHeight;
        }

        @Nullable
        FontTexture.Node insert(IGlyphInfo glyphInfo) {
            if (this.left != null && this.right != null) {
                FontTexture.Node fonttexture$node = this.left.insert(glyphInfo);
                if (fonttexture$node == null) {
                    fonttexture$node = this.right.insert(glyphInfo);
                }

                return fonttexture$node;
            } else if (this.occupied) {
                return null;
            } else {
                int i = glyphInfo.getPixelWidth();
                int j = glyphInfo.getPixelHeight();
                if (i <= this.width && j <= this.height) {
                    if (i == this.width && j == this.height) {
                        this.occupied = true;
                        return this;
                    } else {
                        int k = this.width - i;
                        int l = this.height - j;
                        if (k > l) {
                            this.left = new FontTexture.Node(this.x, this.y, i, this.height);
                            this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
                        } else {
                            this.left = new FontTexture.Node(this.x, this.y, this.width, j);
                            this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
                        }

                        return this.left.insert(glyphInfo);
                    }
                } else {
                    return null;
                }
            }
        }
    }
}