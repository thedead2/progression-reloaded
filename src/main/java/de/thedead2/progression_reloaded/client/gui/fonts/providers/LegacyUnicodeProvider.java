package de.thedead2.progression_reloaded.client.gui.fonts.providers;

import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.client.gui.fonts.glyphs.BakedFontGlyph;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IGlyphInfo;
import de.thedead2.progression_reloaded.api.gui.fonts.glyphs.IUnbakedGlyph;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;


public class LegacyUnicodeProvider implements IFontGlyphProvider {
    private static final int UNICODE_SHEETS = 256;
    private static final int CODEPOINTS_PER_SHEET = 256;
    private static final int TEXTURE_SIZE = 256;
    private static final byte NO_GLYPH = 0;
    private static final int TOTAL_CODEPOINTS = 65536;
    private static final int glyphHeight = 16;
    private final byte[] sizes;
    private final Sheet[] sheets = new Sheet[256];

    public LegacyUnicodeProvider(byte[] sizes, String texturePattern, BiFunction<FileToIdConverter, Predicate<ResourceLocation>, Map<ResourceLocation, Resource>> function) {
        this.sizes = sizes;
        Set<ResourceLocation> set = new HashSet<>();

        for(int i = 0; i < 256; ++i) {
            int j = i * 256;
            set.add(getSheetLocation(texturePattern, j));
        }

        String s = getCommonSearchPrefix(set);
        FileToIdConverter legacyUnicodeIdConverter = new FileToIdConverter(s, ".png");
        Map<ResourceLocation, CompletableFuture<NativeImage>> map = new HashMap<>();
        function.apply(legacyUnicodeIdConverter, set::contains).forEach((resourceLocation, resource) -> {
            map.put(resourceLocation, CompletableFuture.supplyAsync(() -> {
                try {
                    NativeImage nativeimage;
                    try (InputStream inputstream = resource.open()) {
                        nativeimage = NativeImage.read(NativeImage.Format.RGBA, inputstream);
                    }

                    return nativeimage;
                } catch (IOException e) {
                    CrashHandler.getInstance().handleException("Failed to read resource " + resourceLocation + " from pack " + resource.sourcePackId(), e, Level.ERROR);
                    return null;
                }
            }, Util.backgroundExecutor()));
        });

        List<CompletableFuture<?>> list = new ArrayList<>(256);

        for(int k = 0; k < 256; ++k) {
            int l = k * 256;
            int i1 = k;
            ResourceLocation resourcelocation = getSheetLocation(texturePattern, l);
            CompletableFuture<NativeImage> completablefuture = map.get(resourcelocation);
            if (completablefuture != null) {
                list.add(completablefuture.thenAcceptAsync((image) -> {
                    if (image != null) {
                        if (image.getWidth() == 256 && image.getHeight() == 256) {
                            for(int j1 = 0; j1 < 256; ++j1) {
                                byte b0 = sizes[l + j1];
                                if (b0 != 0 && getLeft(b0) > getRight(b0)) {
                                    sizes[l + j1] = 0;
                                }
                            }

                            this.sheets[i1] = new Sheet(sizes, image);
                        } else {
                            image.close();
                            Arrays.fill(sizes, l, l + 256, (byte)0);
                        }

                    }
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(list.toArray(CompletableFuture[]::new)).join();
    }

    private static String getCommonSearchPrefix(Set<ResourceLocation> resourceLocations) {
        String s = StringUtils.getCommonPrefix(resourceLocations.stream().map(ResourceLocation::getPath).toArray(String[]::new));
        int i = s.lastIndexOf("/");
        return i == -1 ? "" : s.substring(0, i);
    }

    private static ResourceLocation getSheetLocation(String texturePattern, int index) {
        String s = String.format(Locale.ROOT, "%02x", index / 256);
        ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, texturePattern, s));
        return resourcelocation.withPrefix("textures/");
    }


    @Override
    public float getScalingFactor(float px) {
        return (px * 2) / glyphHeight;
    }


    @Nullable
    @Override
    public IUnbakedGlyph getUnbakedGlyph(int character) {
        if (character >= 0 && character < this.sizes.length) {
            int i = character / 256;
            Sheet sheet = this.sheets[i];
            return sheet != null ? sheet.getUnbakedGlyph(character) : null;
        } else {
            return null;
        }
    }


    @Override
    public IntSet getSupportedGlyphs() {
        IntSet intset = new IntOpenHashSet();

        for(int i = 0; i < this.sizes.length; ++i) {
            if (this.sizes[i] != 0) {
                intset.add(i);
            }
        }

        return intset;
    }


    @Override
    public void close() {
        for(Sheet sheet : this.sheets) {
            if (sheet != null) {
                sheet.close();
            }
        }
    }

    static int getLeft(byte b) {
        return b >> 4 & 15;
    }

    static int getRight(byte b) {
        return (b & 15) + 1;
    }

    @OnlyIn(Dist.CLIENT)
    record UnbakedGlyph(int sourceX, int sourceY, int width, int height, NativeImage source) implements IUnbakedGlyph {
        public float getAdvance() {
            return (float)(this.width / 2 + 1);
        }


        public float getShadowOffset() {
            return 0.5F;
        }

        public float getBoldOffset() {
            return 0.5F;
        }

        public BakedFontGlyph bake(Function<IGlyphInfo, BakedFontGlyph> function) {
            return function.apply(new IGlyphInfo() {
                public float getOverSample() {
                    return 2.0F;
                }

                public int getPixelWidth() {
                    return UnbakedGlyph.this.width;
                }

                public int getPixelHeight() {
                    return UnbakedGlyph.this.height;
                }

                public void upload(int pXOffset, int pYOffset) {
                    UnbakedGlyph.this.source.upload(0, pXOffset, pYOffset, UnbakedGlyph.this.sourceX, UnbakedGlyph.this.sourceY, UnbakedGlyph.this.width, UnbakedGlyph.this.height, false, false);
                }

                public boolean isColored() {
                    return UnbakedGlyph.this.source.format().components() > 1;
                }
            });
        }
    }

    static class Sheet implements AutoCloseable {
        private final byte[] sizes;
        private final NativeImage source;

        Sheet(byte[] sizes, NativeImage source) {
            this.sizes = sizes;
            this.source = source;
        }

        public void close() {
            this.source.close();
        }

        @Nullable
        public IUnbakedGlyph getUnbakedGlyph(int i) {
            byte b0 = this.sizes[i];
            if (b0 != 0) {
                int left = getLeft(b0);
                return new UnbakedGlyph(left % 16 * 16 + left, (left & 255) / 16 * 16, getRight(b0) - left, glyphHeight, this.source);
            } else {
                return null;
            }
        }
    }
}
