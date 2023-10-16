package de.thedead2.progression_reloaded.api.gui.fonts;

import com.google.gson.JsonObject;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;


@FunctionalInterface
public interface IFontReader<T extends IFontGlyphProvider> {
    T create(JsonObject jsonObject, BiFunction<FileToIdConverter, Predicate<ResourceLocation>, Map<ResourceLocation, Resource>> function);
}
