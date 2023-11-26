package de.thedead2.progression_reloaded.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import stdlib.Strings;


public class NetworkHelper {

    public static <T> void writeTagKey(FriendlyByteBuf buf, TagKey<T> tag) {
        buf.writeUtf(tag.registry().location() + "|" + tag.location());
    }


    @SuppressWarnings("unchecked")
    public static <T> TagKey<T> readTagKey(FriendlyByteBuf buf) {
        String s = buf.readUtf();
        String[] keys = Strings.split(s, '|');
        ResourceLocation registryLocation = new ResourceLocation(keys[0]);
        ResourceLocation tagLocation = new ResourceLocation(keys[1]);
        ResourceKey<Registry<Object>> registryId = ResourceKey.createRegistryKey(registryLocation);

        return (TagKey<T>) TagKey.create(registryId, tagLocation);
    }
}
