package de.thedead2.progression_reloaded.util.registries;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public interface ModRegistriesDynamicSerializer {
    JsonElement toJson();
    ResourceLocation getId();
}
