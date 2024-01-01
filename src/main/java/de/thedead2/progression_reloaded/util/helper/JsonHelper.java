package de.thedead2.progression_reloaded.util.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import joptsimple.internal.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;


public class JsonHelper {

    public static ItemStack itemFromJson(JsonElement jsonElement) {
        ItemStack itemStack;
        if(jsonElement.isJsonPrimitive()) {
            itemStack = new ItemStack(GsonHelper.convertToItem(jsonElement, "item"));
        }
        else {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            itemStack = new ItemStack(GsonHelper.getAsItem(jsonObject, "item"));
            if(jsonObject.has("nbt")) {
                try {
                    CompoundTag compoundtag = TagParser.parseTag(GsonHelper.convertToString(jsonObject.get("nbt"), "nbt"));
                    itemStack.setTag(compoundtag);
                }
                catch(CommandSyntaxException commandsyntaxexception) {
                    throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
                }
            }
        }

        return itemStack;
    }


    public static JsonElement itemToJson(ItemStack item) {
        String itemKey = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
        if(item.hasTag()) {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", itemKey);
            jsonobject.addProperty("nbt", item.getTag().toString());
            return jsonobject;
        }
        else {
            return new JsonPrimitive(itemKey);
        }
    }


    public static JsonObject effectInstanceToJson(MobEffectInstance effect) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("effect", ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString()); //TODO: Better by uuid!!
        jsonObject.addProperty("duration", effect.getDuration());
        jsonObject.addProperty("amplifier", effect.getAmplifier());
        jsonObject.addProperty("ambient", effect.isAmbient());
        jsonObject.addProperty("visible", effect.isVisible());
        jsonObject.addProperty("showIcon", effect.showIcon());

        return jsonObject;
    }


    public static MobEffectInstance effectInstanceFromJson(JsonObject jsonObject) {
        MobEffect effect1;
        int duration, amplifier;
        boolean ambient, visible, showIcon;

        effect1 = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(jsonObject.get("effect").getAsString()));
        duration = jsonObject.get("duration").getAsInt();
        amplifier = jsonObject.get("amplifier").getAsInt();
        ambient = jsonObject.get("ambient").getAsBoolean();
        visible = jsonObject.get("visible").getAsBoolean();
        showIcon = jsonObject.get("showIcon").getAsBoolean();


        return new MobEffectInstance(effect1, duration, amplifier, ambient, visible, showIcon);
    }


    public static <T extends Enum<T>> void writeEnum(T enumValue, String memberName, JsonObject jsonObject) {
        jsonObject.addProperty(memberName, enumValue.name());
    }


    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String memberName, JsonObject jsonObject) {
        return T.valueOf(enumClass, jsonObject.get(memberName).getAsString());
    }

    public static String formatJsonObject(JsonElement jsonElement) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = jsonElement.toString().toCharArray();
        int i = 0;
        for(int j = 0; j < chars.length; j++) {
            char c = chars[j];
            char previousChar = j - 1 < 0 ? c : chars[j - 1];
            char nextChar = j + 1 >= chars.length ? c : chars[j + 1];

            if(c == '{') {
                stringBuilder.append(c);
                if(nextChar != '}') {
                    i++;
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if(c == '}') {
                if(previousChar != '{') {
                    i--;
                    stringBuilder.append("\n").append(Strings.repeat('\t', i));
                }
                stringBuilder.append(c);
                if(nextChar != ',' && nextChar != '\"' && nextChar != '\'' && nextChar != '}' && nextChar != ']') {
                    stringBuilder.append('\n').append(Strings.repeat('\t', i));
                }
            }
            else if(c == ',') {
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if(c == '[' && (nextChar == '\"' || nextChar == '[')) {
                i++;
                stringBuilder.append(c).append('\n').append(Strings.repeat('\t', i));
            }
            else if(c == ']' && (previousChar == '\"' || previousChar == ']')) {
                i--;
                stringBuilder.append('\n').append(Strings.repeat('\t', i)).append(c);
            }
            else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}
