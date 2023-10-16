package de.thedead2.progression_reloaded.client.gui.fonts.readers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.fonts.providers.LegacyUnicodeProvider;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;


public class LegacyUnicodeFontReader implements IFontReader<LegacyUnicodeProvider> {
    @Override
    public LegacyUnicodeProvider create(JsonObject jsonObject, BiFunction<FileToIdConverter, Predicate<ResourceLocation>, Map<ResourceLocation, Resource>> function) {
        ResourceLocation metadata = new ResourceLocation(GsonHelper.getAsString(jsonObject, "sizes"));
        String texturePattern = getTemplate(jsonObject);

        try {
            LegacyUnicodeProvider provider;
            try (InputStream inputstream = Minecraft.getInstance().getResourceManager().open(metadata)) {
                byte[] abyte = inputstream.readNBytes(65536);
                provider = new LegacyUnicodeProvider(abyte, texturePattern, function);
            }

            return provider;
        } catch (IOException e) {
            CrashHandler.getInstance().handleException("Cannot load " + metadata + ", unicode glyphs will not render correctly", e, Level.ERROR);
            return null;
        }
    }

    private static String getTemplate(JsonObject jsonObject) {
        String s = GsonHelper.getAsString(jsonObject, "template");

        try {
            String.format(Locale.ROOT, s, "");
            return s;
        } catch (IllegalFormatException illegalformatexception) {
            throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + s);
        }
    }
}
