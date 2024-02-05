package de.thedead2.progression_reloaded.network;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import stdlib.Strings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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


    public static <T> void writeArray(T[] array, FriendlyByteBuf buf, FriendlyByteBuf.Writer<T> elementWriter) {
        buf.writeCollection(List.of(array), elementWriter);
    }


    @SuppressWarnings("unchecked")
    public static <T> T[] readArray(FriendlyByteBuf buf, FriendlyByteBuf.Reader<T> elementReader) {
        Collection<T> collection = buf.readCollection(ArrayList::new, elementReader);

        return (T[]) collection.toArray(Object[]::new);
    }


    @SuppressWarnings("unchecked")
    public static <T> T createGeneric(Class<? extends T> aClass, FriendlyByteBuf buf) {
        try {
            Method fromNBT = aClass.getDeclaredMethod("fromNetwork", FriendlyByteBuf.class);
            return (T) fromNBT.invoke(null, buf);
        }
        catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
