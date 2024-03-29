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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@MethodsReturnNonnullByDefault
public class CollectionHelper {


    public static <T, R, V> HashMap<R, V> convertMapKeys(Map<T, V> oldMap, Function<T, R> keyConverter) {
        return convertMapKeys(oldMap, Maps::newHashMapWithExpectedSize, keyConverter);
    }


    public static <T, R, V, U extends Map<R, V>> U convertMapKeys(Map<T, V> oldMap, @NotNull IntFunction<U> mapFactory, Function<T, R> keyConverter) {
        U newMap = mapFactory.apply(oldMap.size());
        oldMap.forEach((t, v) -> {
            R r = keyConverter.apply(t);
            newMap.put(r, v);
        });
        return newMap;
    }


    public static <T, R, V> HashMap<T, V> convertMapValues(Map<T, R> oldMap, Function<R, V> valueConverter) {
        return convertMapValues(oldMap, Maps::newHashMapWithExpectedSize, valueConverter);
    }


    public static <T, R, V, U extends Map<T, V>> U convertMapValues(Map<T, R> oldMap, @NotNull IntFunction<U> mapFactory, Function<R, V> valueConverter) {
        U newMap = mapFactory.apply(oldMap.size());
        oldMap.forEach((t, r) -> {
            V v = valueConverter.apply(r);
            newMap.put(t, v);
        });
        return newMap;
    }


    public static <T, R, V, W> HashMap<T, R> convertMap(Map<V, W> oldMap, Function<V, T> keyConverter, Function<W, R> valueConverter) {
        return convertMap(oldMap, Maps::newHashMapWithExpectedSize, keyConverter, valueConverter);
    }


    public static <T, R, V, W, X extends Map<T, R>> X convertMap(Map<V, W> oldMap, @NotNull IntFunction<X> mapFactory, Function<V, T> keyConverter, Function<W, R> valueConverter) {
        X newMap = mapFactory.apply(oldMap.size());
        oldMap.forEach((v, w) -> {
            T t = keyConverter.apply(v);
            R r = valueConverter.apply(w);

            newMap.put(t, r);
        });
        return newMap;
    }


    public static <T, V> List<V> convertCollection(Collection<T> oldCollection, Function<T, V> valueConverter) {
        return convertCollection(oldCollection, Lists::newArrayListWithExpectedSize, valueConverter);
    }


    public static <T, V, R extends Collection<V>> R convertCollection(Collection<T> oldCollection, @NotNull IntFunction<R> collectionFactory, Function<T, V> valueConverter) {
        R newCollection = collectionFactory.apply(oldCollection.size());
        oldCollection.forEach(t -> {
            V v = valueConverter.apply(t);
            newCollection.add(v);
        });
        return newCollection;
    }


    public static <T, V extends Collection<T>> V filterCollection(V collection, Predicate<T> filter) {
        collection.removeIf(t -> !filter.test(t));

        return collection;
    }


    public static <T, V, R extends Map<T, V>> R filterMap(R map, BiPredicate<T, V> filter) {
        map.entrySet().removeIf(entry -> !filter.test(entry.getKey(), entry.getValue()));
        return map;
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


    public static <T> ListTag saveToNBT(T[] collection, Function<T, Tag> valueConverter) {
        ListTag listTag = new ListTag();
        for(T t : collection) {
            listTag.add(valueConverter.apply(t));
        }

        return listTag;
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
        return loadFromNBT(Maps::newHashMapWithExpectedSize, tag, keyConverter, valueConverter);
    }


    public static <T, V, R extends Map<T, V>> R loadFromNBT(IntFunction<R> mapFactory, CompoundTag tag, Function<String, T> keyConverter, Function<Tag, V> valueConverter) {
        R map = mapFactory.apply(tag.size());
        tag.getAllKeys().forEach(s -> map.put(keyConverter.apply(s), valueConverter.apply(tag.get(s))));

        return map;
    }


    public static <T> List<T> loadFromNBT(ListTag tag, Function<Tag, T> valueConverter) {
        return loadFromNBT(Lists::newArrayListWithExpectedSize, tag, valueConverter);
    }


    public static <T, V extends Collection<T>> V loadFromNBT(IntFunction<V> collectionFactory, ListTag tag, Function<Tag, T> valueConverter) {
        V collection = collectionFactory.apply(tag.size());
        tag.forEach(tag1 -> collection.add(valueConverter.apply(tag1)));

        return collection;
    }


    public static <T> JsonArray saveToJson(T[] collection, Function<T, JsonElement> valueConverter) {
        JsonArray jsonArray = new JsonArray(collection.length);

        for(T t : collection) {
            jsonArray.add(valueConverter.apply(t));
        }

        return jsonArray;
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
        return loadFromJson(Lists::newArrayListWithExpectedSize, jsonArray, valueConverter);
    }


    public static <T> T[] loadFromJson(Class<T> typeClass, JsonArray jsonArray, Function<JsonElement, T> valueConverter) {
        T[] array = (T[]) typeClass.arrayType().cast(new Object[jsonArray.size()]);
        for(int i = 0; i < jsonArray.size(); i++) {
            array[i] = valueConverter.apply(jsonArray.get(i));
        }

        return array;
    }

    public static <T, R extends Collection<T>> R loadFromJson(IntFunction<R> collectionFactory, JsonArray jsonArray, Function<JsonElement, T> valueConverter) {
        R collection = collectionFactory.apply(jsonArray.size());
        jsonArray.forEach(jsonElement -> collection.add(valueConverter.apply(jsonElement)));

        return collection;
    }


    public static <T, V> HashMap<T, V> loadFromJson(JsonObject jsonObject, Function<String, T> keyConverter, Function<JsonElement, V> valueConverter) {
        return loadFromJson(Maps::newHashMapWithExpectedSize, jsonObject, keyConverter, valueConverter);
    }


    public static <T, V, R extends Map<T, V>> R loadFromJson(IntFunction<R> mapFactory, JsonObject jsonObject, Function<String, T> keyConverter, Function<JsonElement, V> valueConverter) {
        R map = mapFactory.apply(jsonObject.size());
        jsonObject.asMap().forEach((s, jsonElement) -> map.put(keyConverter.apply(s), valueConverter.apply(jsonElement)));

        return map;
    }


    @SafeVarargs
    public static <T> Collection<T> concatenate(Collection<T> targetCollection, Collection<T>... collections) {
        for(Collection<T> collection : collections) {
            targetCollection.addAll(collection);
        }

        return targetCollection;
    }


    @SafeVarargs
    public static <T, V> Map<T, V> concatenate(Map<T, V> targetMap, Map<T, V>... maps) {
        for(Map<T, V> map : maps) {
            targetMap.putAll(map);
        }

        return targetMap;
    }
}
