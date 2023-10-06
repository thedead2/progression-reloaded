package de.thedead2.progression_reloaded.api;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;


public interface IProgressable {
    JsonElement toJson();
    ResourceLocation getId();
}
