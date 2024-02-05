package de.thedead2.progression_reloaded.api;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import net.minecraft.resources.ResourceLocation;


public interface IProgressable<T extends IProgressable<T>> extends Comparable<T>, INetworkSerializable, IJsonSerializable {
    JsonElement toJson();
    ResourceLocation getId();
}
