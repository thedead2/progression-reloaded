package de.thedead2.progression_reloaded.util.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public abstract class CollectionHelper {

    public static <T, R, V> Map<R, V> convertMapKeys(Map<T, V> oldMap, Function<T, R> keyConversionFunction) {
        return convertMapKeys(oldMap, new HashMap<>(), keyConversionFunction);
    }


    public static <T, R, V> Map<R, V> convertMapKeys(Map<T, V> oldMap, Map<R, V> newMap, Function<T, R> keyConversionFunction) {
        oldMap.forEach((t, v) -> {
            R r = keyConversionFunction.apply(t);
            newMap.put(r, v);
        });
        return newMap;
    }


    public static <T, R, V> Map<T, V> convertMapValues(Map<T, R> oldMap, Function<R, V> valueConversionFunction) {
        return convertMapValues(oldMap, new HashMap<>(), valueConversionFunction);
    }


    public static <T, R, V> Map<T, V> convertMapValues(Map<T, R> oldMap, Map<T, V> newMap, Function<R, V> valueConversionFunction) {
        oldMap.forEach((t, r) -> {
            V v = valueConversionFunction.apply(r);
            newMap.put(t, v);
        });
        return newMap;
    }


    public static <T, R, V, W> Map<T, R> convertMap(Map<V, W> oldMap, Function<V, T> keyConversionFunction, Function<W, R> valueConversionFunction) {
        return convertMap(oldMap, new HashMap<>(), keyConversionFunction, valueConversionFunction);
    }


    public static <T, R, V, W> Map<T, R> convertMap(Map<V, W> oldMap, Map<T, R> newMap, Function<V, T> keyConversionFunction, Function<W, R> valueConversionFunction) {
        oldMap.forEach((v, w) -> {
            T t = keyConversionFunction.apply(v);
            R r = valueConversionFunction.apply(w);

            newMap.put(t, r);
        });
        return newMap;
    }


    public static <T, V> Collection<V> convertCollection(Collection<T> oldCollection, Collection<V> newCollection, Function<T, V> valueConversionFunction) {
        oldCollection.forEach(t -> {
            V v = valueConversionFunction.apply(t);
            newCollection.add(v);
        });
        return newCollection;
    }
}
