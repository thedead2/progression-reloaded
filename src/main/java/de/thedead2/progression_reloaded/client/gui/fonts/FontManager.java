package de.thedead2.progression_reloaded.client.gui.fonts;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontGlyphProvider;
import de.thedead2.progression_reloaded.client.gui.fonts.readers.*;
import de.thedead2.progression_reloaded.client.gui.fonts.types.MissingFont;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.exceptions.UnknownFontTypeException;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class FontManager extends SimplePreparableReloadListener<Map<ResourceLocation, Multimap<ResourceLocation, JsonObject>>> implements AutoCloseable {
    private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
    private static final Marker MARKER = new MarkerManager.Log4jMarker("FontManager");
    private final Map<ResourceLocation, IFontReader<?>> fontReaders = new HashMap<>();
    private final Map<ResourceLocation, ProgressionFont> fonts = new HashMap<>();
    private ProgressionFont missingFont;

    public FontManager() {
        this.fontReaders.putAll(PREventFactory.onFontTypeRegistration());
        this.registerDefaultReaders();
    }

    private void registerDefaultReaders() {
        this.fontReaders.put(new ResourceLocation(MOD_ID, "ttf"), new TTFReader());
        this.fontReaders.put(new ResourceLocation(MOD_ID, "bitmap"), new BitmapFontReader());
        this.fontReaders.put(new ResourceLocation(MOD_ID, "space"), SpecialReaders.SPACE_READER);
        this.fontReaders.put(new ResourceLocation(MOD_ID, "legacy_unicode"), new LegacyUnicodeFontReader());
    }


    //TODO: support multiple fonts for italic, bold, alt, etc.
    @Override
    protected @NotNull Map<ResourceLocation, Multimap<ResourceLocation, JsonObject>> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        Map<ResourceLocation, Multimap<ResourceLocation, JsonObject>> fonts = Maps.newHashMap();

        for(Map.Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation fileName = FONT_DEFINITIONS.fileToId(entry.getKey());
            Multimap<ResourceLocation, JsonObject> readers = HashMultimap.create();

            for(Resource resource : entry.getValue()) {
                try(Reader reader = resource.openAsReader()) {
                    JsonObject jsonObject = GsonHelper.fromJson(gson, reader, JsonObject.class);

                    JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonObject, "providers");

                    for(int i = jsonarray.size() - 1; i >= 0; --i) {
                        JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonarray.get(i), "providers[" + i + "]");
                        String s = GsonHelper.getAsString(jsonobject, "type");

                        ResourceLocation readerId = new ResourceLocation(MOD_ID, s);
                        readers.put(readerId, jsonobject);
                    }
                }
                catch(IOException e) {
                    CrashHandler.getInstance().handleException("Failed to read font: " + resource.sourcePackId(), MARKER, e, Level.ERROR);
                }
            }

            fonts.put(fileName, readers);
        }
        return fonts;
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, Multimap<ResourceLocation, JsonObject>> fonts, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.fonts.clear();

        this.missingFont = new MissingFont();
        this.fonts.put(this.missingFont.getName(), this.missingFont);

        fonts.forEach((fontName, readers) -> {
            try {
                List<IFontGlyphProvider> glyphProviders = Lists.newArrayList();

                for(Map.Entry<ResourceLocation, Collection<JsonObject>> entry : readers.asMap().entrySet()) {
                    ResourceLocation readerId = entry.getKey();
                    Collection<JsonObject> jsonObjects = entry.getValue();

                    IFontReader<?> fontReader = this.fontReaders.get(readerId);

                    if(fontReader == null) {
                        throw new UnknownFontTypeException(readerId);
                    }

                    for(JsonObject jsonObject : jsonObjects) {
                        IFontGlyphProvider glyphProvider = fontReader.create(jsonObject, (fileToIdConverter, locationPredicate) -> {
                            var map = fileToIdConverter.listMatchingResources(resourceManager);
                            var entries = map.entrySet().stream()
                                             .filter(entry1 -> locationPredicate.test(entry1.getKey()))
                                             .toArray(Map.Entry[]::new);
                            return Map.ofEntries(entries);
                        });
                        glyphProviders.add(glyphProvider);
                    }
                }

                if(glyphProviders.isEmpty()) {
                    throw new IllegalStateException("No glyph providers found for font: " + fontName);
                }

                this.cacheFontGlyphs(glyphProviders);

                this.fonts.put(fontName, new ProgressionFont(fontName, glyphProviders));
            }
            catch(Throwable e) {
                CrashHandler.getInstance().handleException("Failed to load font: " + fontName, MARKER, e, Level.ERROR);
            }
        });

        LOGGER.info(MARKER, "Successfully loaded {} fonts", this.fonts.size());
    }

    private void cacheFontGlyphs(List<IFontGlyphProvider> glyphProviders) {
        IntSet intset = new IntOpenHashSet();

        for(IFontGlyphProvider glyphProvider : glyphProviders) {
            intset.addAll(glyphProvider.getSupportedGlyphs());
        }

        intset.forEach((int i) -> {
            if (i != 32) {
                for(IFontGlyphProvider glyphProvider : Lists.reverse(glyphProviders)) {
                    if (glyphProvider.getUnbakedGlyph(i) != null) {
                        break;
                    }
                }
            }
        });
    }


    @Override
    public @NotNull String getName() {
        return MARKER.getName();
    }


    @NotNull
    public static ProgressionFont getFont(ResourceLocation fontId) {
        FontManager fontManager = ModClientInstance.getInstance().getFontManager();
        ProgressionFont font = fontManager.fonts.get(fontId);
        return font == null ? fontManager.missingFont.defaultFormatting() : font.defaultFormatting();
    }

    @Override
    public void close() {
        this.fonts.values().forEach(ProgressionFont::close);
        this.missingFont.close();
    }
}
