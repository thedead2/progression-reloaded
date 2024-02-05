package de.thedead2.progression_reloaded.api;

import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import de.thedead2.progression_reloaded.data.RestrictionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.stream.Stream;


public interface IRestrictionType<T> extends INetworkSerializable {

    static <T> Pair<ResourceLocation, Stream<TagKey<T>>> getFromRegistry(IForgeRegistry<T> registry, T object) {
        ResourceLocation id = registry.getKey(object);
        Stream<TagKey<T>> tags;
        ITagManager<T> tagManager = registry.tags();
        if(tagManager != null) {
            tags = tagManager.getTagNames();
        }
        else {
            tags = Stream.empty();
        }

        return Pair.of(id, tags);
    }

    @SuppressWarnings("unchecked")
    static <R> IRestrictionType<R> fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation(); //FIXME: tried to read component as resource location for dimension restriction --> why?
        return (IRestrictionType<R>) RestrictionManager.RESTRICTION_TYPES.get(id);
    }

    Pair<ResourceLocation, Stream<TagKey<T>>> get(T object);

    @Override
    default void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(RestrictionManager.RESTRICTION_TYPES.inverse().get(this));
    }
}
