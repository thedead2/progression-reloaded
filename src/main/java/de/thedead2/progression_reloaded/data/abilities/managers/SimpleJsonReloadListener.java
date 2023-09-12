package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;

import java.util.Map;


public abstract class SimpleJsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
}
