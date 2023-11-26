package de.thedead2.progression_reloaded.util.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@MethodsReturnNonnullByDefault
public class CollectionHelper {


    public static <T, R, V> HashMap<R, V> convertMapKeys(Map<T, V> oldMap, Function<T, R> keyConverter) {
        return convertMapKeys(oldMap, new HashMap<>(), keyConverter);
    }


    public static <T, R, V, U extends Map<R, V>> U convertMapKeys(Map<T, V> oldMap, @NotNull U newMap, Function<T, R> keyConverter) {
        oldMap.forEach((t, v) -> {
            R r = keyConverter.apply(t);
            newMap.put(r, v);
        });
        return newMap;
    }


    public static <T, R, V> HashMap<T, V> convertMapValues(Map<T, R> oldMap, Function<R, V> valueConverter) {
        return convertMapValues(oldMap, new HashMap<>(), valueConverter);
    }


    public static <T, R, V, U extends Map<T, V>> U convertMapValues(Map<T, R> oldMap, @NotNull U newMap, Function<R, V> valueConverter) {
        oldMap.forEach((t, r) -> {
            V v = valueConverter.apply(r);
            newMap.put(t, v);
        });
        return newMap;
    }


    public static <T, R, V, W> HashMap<T, R> convertMap(Map<V, W> oldMap, Function<V, T> keyConverter, Function<W, R> valueConverter) {
        return convertMap(oldMap, new HashMap<>(), keyConverter, valueConverter);
    }


    public static <T, R, V, W, X extends Map<T, R>> X convertMap(Map<V, W> oldMap, @NotNull X newMap, Function<V, T> keyConverter, Function<W, R> valueConverter) {
        oldMap.forEach((v, w) -> {
            T t = keyConverter.apply(v);
            R r = valueConverter.apply(w);

            newMap.put(t, r);
        });
        return newMap;
    }


    public static <T, V> List<V> convertCollection(Collection<T> oldCollection, Function<T, V> valueConverter) {
        return convertCollection(oldCollection, Lists.newArrayListWithExpectedSize(oldCollection.size()), valueConverter);
    }


    public static <T, V, R extends Collection<V>> R convertCollection(Collection<T> oldCollection, @NotNull R newCollection, Function<T, V> valueConverter) {
        oldCollection.forEach(t -> {
            V v = valueConverter.apply(t);
            newCollection.add(v);
        });
        return newCollection;
    }


    public static <T> Optional<T> findObjectWithHighestCount(Collection<T> collection) {
        Map<T, Long> objectGroups = collection.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return objectGroups.entrySet()
                           .stream()
                           .sorted(Map.Entry.<T, Long>comparingByValue().reversed())
                           .limit(1)
                           .map(Map.Entry::getKey)
                           .findFirst();
    }


    public static <T> ListTag saveToNBT(Collection<T> collection, Function<T, Tag> valueConverter) {
        ListTag tag = new ListTag();
        collection.forEach(t -> tag.add(valueConverter.apply(t)));

        return tag;
    }


    public static <T, V> CompoundTag saveToNBT(Map<T, V> map, Function<T, String> keyConverter, Function<V, Tag> valueConverter) {
        CompoundTag tag = new CompoundTag();
        map.forEach((t, v) -> tag.put(keyConverter.apply(t), valueConverter.apply(v)));
        return tag;
    }


    public static <T, V> HashMap<T, V> loadFromNBT(CompoundTag tag, Function<String, T> keyConverter, Function<Tag, V> valueConverter) {
        return loadFromNBT(Maps.newHashMapWithExpectedSize(tag.size()), tag, keyConverter, valueConverter);
    }


    public static <T, V, R extends Map<T, V>> R loadFromNBT(R map, CompoundTag tag, Function<String, T> keyConverter, Function<Tag, V> valueConverter) {
        tag.getAllKeys().forEach(s -> map.put(keyConverter.apply(s), valueConverter.apply(tag.get(s))));

        return map;
    }


    public static <T> List<T> loadFromNBT(ListTag tag, Function<Tag, T> valueConverter) {
        return loadFromNBT(Lists.newArrayListWithExpectedSize(tag.size()), tag, valueConverter);
    }


    public static <T, V extends Collection<T>> V loadFromNBT(V collection, ListTag tag, Function<Tag, T> valueConverter) {
        tag.forEach(tag1 -> collection.add(valueConverter.apply(tag1)));

        return collection;
    }


    public static <T> JsonArray saveToJson(Collection<T> collection, Function<T, JsonElement> valueConverter) {
        JsonArray jsonArray = new JsonArray(collection.size());
        collection.forEach(t -> jsonArray.add(valueConverter.apply(t)));

        return jsonArray;
    }


    public static <T, V> JsonObject saveToJson(Map<T, V> map, Function<T, String> keyConverter, Function<V, JsonElement> valueConverter) {
        JsonObject jsonObject = new JsonObject();
        map.forEach((t, v) -> jsonObject.add(keyConverter.apply(t), valueConverter.apply(v)));

        return jsonObject;
    }


    public static <T> List<T> loadFromJson(JsonArray jsonArray, Function<JsonElement, T> valueConverter) {
        return loadFromJson(Lists.newArrayListWithExpectedSize(jsonArray.size()), jsonArray, valueConverter);
    }


    public static <T, R extends Collection<T>> R loadFromJson(R collection, JsonArray jsonArray, Function<JsonElement, T> valueConverter) {
        jsonArray.forEach(jsonElement -> collection.add(valueConverter.apply(jsonElement)));

        return collection;
    }


    public static <T, V> HashMap<T, V> loadFromJson(JsonObject jsonObject, Function<String, T> keyConverter, Function<JsonElement, V> valueConverter) {
        return loadFromJson(Maps.newHashMapWithExpectedSize(jsonObject.size()), jsonObject, keyConverter, valueConverter);
    }


    public static <T, V, R extends Map<T, V>> R loadFromJson(R map, JsonObject jsonObject, Function<String, T> keyConverter, Function<JsonElement, V> valueConverter) {
        jsonObject.asMap().forEach((s, jsonElement) -> map.put(keyConverter.apply(s), valueConverter.apply(jsonElement)));

        return map;
    }
}
