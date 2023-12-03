package de.thedead2.progression_reloaded.util.helper;

import com.google.gson.*;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


/**
 * Utility class for easy conversion of {@link JsonElement} to {@link Tag} and vice versa.
 **/
public class SerializationHelper {

    public static Tag convertToNBT(JsonElement jsonElement) {
        if(jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if(primitive.isBoolean() || primitive.isString()) {
                return StringTag.valueOf(primitive.getAsString());
            }
            else {
                Number number = primitive.getAsNumber();
                if(number instanceof Byte b) {
                    return ByteTag.valueOf(b);
                }
                else if(number instanceof Integer i) {
                    return IntTag.valueOf(i);
                }
                else if(number instanceof Long l) {
                    return LongTag.valueOf(l);
                }
                else if(number instanceof Short s) {
                    return ShortTag.valueOf(s);
                }
                else if(number instanceof Double d) {
                    return DoubleTag.valueOf(d);
                }
                else if(number instanceof Float f) {
                    return FloatTag.valueOf(f);
                }
                else {
                    return StringTag.valueOf(number.toString());
                }
            }
        }
        else if(jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            ListTag listTag = new ListTag();
            jsonArray.forEach(jsonElement1 -> listTag.add(convertToNBT(jsonElement1)));

            return listTag;
        }
        else if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            CompoundTag tag = new CompoundTag();
            jsonObject.asMap().forEach((s, jsonElement1) -> tag.put(s, convertToNBT(jsonElement1)));

            return tag;
        }
        else {
            return EndTag.INSTANCE;
        }
    }


    public static JsonElement convertToJson(Tag tag) {
        if(tag instanceof StringTag stringTag) {
            String s = stringTag.getAsString();
            if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
                return new JsonPrimitive(Boolean.parseBoolean(s));
            }
            else {
                return new JsonPrimitive(s);
            }
        }
        else if(tag instanceof NumericTag numericTag) {
            return new JsonPrimitive(numericTag.getAsNumber());
        }
        else if(tag instanceof ListTag listTag) {
            JsonArray jsonArray = new JsonArray(listTag.size());
            listTag.forEach(tag1 -> jsonArray.add(convertToJson(tag1)));

            return jsonArray;
        }
        else if(tag instanceof CompoundTag compoundTag) {
            JsonObject jsonObject = new JsonObject();
            compoundTag.getAllKeys().forEach(s -> jsonObject.add(s, convertToJson(compoundTag.get(s))));

            return jsonObject;
        }
        else {
            return JsonNull.INSTANCE;
        }
    }


    public static <T> void addNullable(@Nullable T object, CompoundTag tag, String name, Function<T, Tag> valueConverter) {
        if(object != null) {
            tag.put(name, valueConverter.apply(object));
        }
    }


    public static <T> void addNullable(@Nullable T object, JsonObject jsonObject, String name, Function<T, JsonElement> valueConverter) {
        if(object != null) {
            jsonObject.add(name, valueConverter.apply(object));
        }
    }


    @Nullable
    public static <T> T getNullable(CompoundTag tag, String member, Function<Tag, T> valueConverter) {
        T t = null;
        if(tag.contains(member)) {
            t = valueConverter.apply(tag.get(member));
        }

        return t;
    }


    @Nullable
    public static <T> T getNullable(JsonObject jsonObject, String member, Function<JsonElement, T> valueConverter) {
        T t = null;
        if(jsonObject.has(member)) {
            t = valueConverter.apply(jsonObject.get(member));
        }

        return t;
    }

}
