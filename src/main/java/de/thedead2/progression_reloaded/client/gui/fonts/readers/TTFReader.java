package de.thedead2.progression_reloaded.client.gui.fonts.readers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.TextureUtil;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.fonts.providers.TTFProvider;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;


public class TTFReader implements IFontReader<TTFProvider> {
    private static final FileToIdConverter TTF_ID_CONVERTER = new FileToIdConverter("font", ".ttf");
    private ResourceLocation fileName;
    private float overSample, shiftX, shiftY, size;
    private String skip;

    public void readInfoFromJson(JsonObject jsonObject) {
        this.fileName = null;
        this.overSample = 1;
        this.shiftX = this.shiftY = 0;
        this.size = 11;
        this.skip = "";

        if (jsonObject.has("shift")) {
            JsonArray shiftArray = jsonObject.getAsJsonArray("shift");
            if (shiftArray.size() != 2) {
                throw new JsonParseException("Expected 2 elements in 'shift', found " + shiftArray.size());
            }

            this.shiftX = GsonHelper.convertToFloat(shiftArray.get(0), "shift[0]");
            this.shiftY = GsonHelper.convertToFloat(shiftArray.get(1), "shift[1]");
        }

        StringBuilder stringbuilder = new StringBuilder();
        if (jsonObject.has("skip")) {
            JsonElement jsonelement = jsonObject.get("skip");
            if (jsonelement.isJsonArray()) {
                JsonArray skipArray = GsonHelper.convertToJsonArray(jsonelement, "skip");

                for(int i = 0; i < skipArray.size(); ++i) {
                    stringbuilder.append(GsonHelper.convertToString(skipArray.get(i), "skip[" + i + "]"));
                }
            } else {
                stringbuilder.append(GsonHelper.convertToString(jsonelement, "skip"));
            }
        }
        if(jsonObject.has("oversample")) {
            this.overSample = jsonObject.get("oversample").getAsFloat();
        }

        this.fileName = new ResourceLocation(GsonHelper.getAsString(jsonObject, "file")).withPrefix("font/");
        this.size = GsonHelper.getAsFloat(jsonObject, "size", 11.0F);
        this.skip = stringbuilder.toString();
    }

    public TTFProvider readFontFiles(Map<ResourceLocation, Resource> resources) {
        STBTTFontinfo stbttfontinfo = null;
        ByteBuffer bytebuffer = null;

        Resource resource = resources.values().stream().findFirst().orElseThrow();

        try(InputStream inputStream = resource.open()) {
            TTFProvider TTFProvider;
            stbttfontinfo = STBTTFontinfo.malloc();
            bytebuffer = TextureUtil.readResource(inputStream);
            bytebuffer.flip();

            if (!STBTruetype.stbtt_InitFont(stbttfontinfo, bytebuffer)) {
                throw new IOException("Invalid '.ttf' file!");
            }

            TTFProvider = new TTFProvider(bytebuffer, stbttfontinfo, this.size, this.overSample, this.shiftX, this.shiftY, this.skip);

            return TTFProvider;
        } catch (Exception e) {
            CrashHandler.getInstance().handleException("Failed to read '.ttf' file!", e, Level.ERROR);
            if (stbttfontinfo != null) {
                stbttfontinfo.free();
            }

            MemoryUtil.memFree(bytebuffer);
            return null;
        }
    }


    @Override
    public TTFProvider create(JsonObject jsonObject, BiFunction<FileToIdConverter, Predicate<ResourceLocation>, Map<ResourceLocation, Resource>> function) {
        this.readInfoFromJson(jsonObject);
        Map<ResourceLocation, Resource> resources = function.apply(TTF_ID_CONVERTER, resourceLocation -> resourceLocation.equals(this.fileName));

        return this.readFontFiles(resources);
    }
}
