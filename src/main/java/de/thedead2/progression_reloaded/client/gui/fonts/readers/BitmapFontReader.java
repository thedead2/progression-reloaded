package de.thedead2.progression_reloaded.client.gui.fonts.readers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.NativeImage;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.fonts.providers.BitmapProvider;
import de.thedead2.progression_reloaded.util.ModHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;


public class BitmapFontReader implements IFontReader<BitmapProvider> {
    private static final FileToIdConverter BIT_MAP_ID_CONVERTER = new FileToIdConverter("textures/font", ".png");
    private ResourceLocation fileName;
    private List<int[]> chars;
    private int height;
    private int ascent;

    private void readJsonInfo(JsonObject jsonObject) {
        this.height = GsonHelper.getAsInt(jsonObject, "height", 8);
        this.ascent = GsonHelper.getAsInt(jsonObject, "ascent");
        if (this.ascent > this.height) {
            throw new JsonParseException("Ascent " + this.ascent + " higher than height " + this.height);
        } else {
            this.chars = Lists.newArrayList();
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonObject, "chars");

            for(int k = 0; k < jsonarray.size(); ++k) {
                String s = GsonHelper.convertToString(jsonarray.get(k), "chars[" + k + "]");
                int[] codePoints = s.codePoints().toArray();
                if (k > 0) {
                    int l = this.chars.get(0).length;
                    if (codePoints.length != l) {
                        throw new JsonParseException("Elements of chars have to be the same length (found: " + codePoints.length + ", expected: " + l + "), pad with space or \\u0000");
                    }
                }

                this.chars.add(codePoints);
            }

            if (!this.chars.isEmpty() && this.chars.get(0).length != 0) {
                this.fileName = new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")).withPrefix("textures/");
            }
            else {
                throw new JsonParseException("Expected to find data in chars, found none.");
            }
        }
    }

    @Override
    public BitmapProvider create(JsonObject jsonObject, BiFunction<FileToIdConverter, Predicate<ResourceLocation>, Map<ResourceLocation, Resource>> function) {
        this.readJsonInfo(jsonObject);
        Map<ResourceLocation, Resource> resources = function.apply(BIT_MAP_ID_CONVERTER, resourceLocation -> resourceLocation.equals(this.fileName));
        return this.readFontFiles(resources);
    }

    public BitmapProvider readFontFiles(Map<ResourceLocation, Resource> resources) {
        Resource resource = resources.values().stream().findFirst().orElseThrow();
        try {
            BitmapProvider bitmapProvider;
            try (InputStream inputstream = resource.open()) {
                NativeImage nativeimage = NativeImage.read(NativeImage.Format.RGBA, inputstream);
                int imageWidth = nativeimage.getWidth();
                int imageHeight = nativeimage.getHeight();
                int charWidth = imageWidth / this.chars.get(0).length;
                int charHeight = imageHeight / this.chars.size();
                float scale = (float)this.height / (float)charHeight;

                Int2ObjectMap<BitmapProvider.UnbakedGlyph> glyphs = new Int2ObjectOpenHashMap<>();

                for(int row = 0; row < this.chars.size(); ++row) {
                    int j1 = 0;

                    for(int k1 : this.chars.get(row)) {
                        int column = j1++;
                        if (k1 != 0) {
                            int actualGlyphWidth = this.getActualGlyphWidth(nativeimage, charWidth, charHeight, column, row);
                            BitmapProvider.UnbakedGlyph glyph = glyphs.put(k1, new BitmapProvider.UnbakedGlyph(scale, nativeimage, column * charWidth, row * charHeight, charWidth, charHeight, (int) (0.5D + (actualGlyphWidth * scale)) + 1, this.ascent));
                            if (glyph != null) {
                                ModHelper.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(k1), this.fileName);
                            }
                        }
                    }
                }

                bitmapProvider = new BitmapProvider(nativeimage, glyphs);
            }

            return bitmapProvider;
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception.getMessage());
        }
    }

    private int getActualGlyphWidth(NativeImage image, int charWidth, int charHeight, int column, int row) {
        int i;
        for(i = charWidth - 1; i >= 0; --i) {
            int xPos = column * charWidth + i;

            for(int k = 0; k < charHeight; ++k) {
                int yPos = row * charHeight + k;
                if (image.getLuminanceOrAlpha(xPos, yPos) != 0) {
                    return i + 1;
                }
            }
        }

        return i + 1;
    }
}
