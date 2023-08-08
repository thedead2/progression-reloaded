package de.thedead2.progression_reloaded.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class JsonHelper {

    public static ItemStack itemFromJson(JsonObject pJson) {
        if (!pJson.has("item")) {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        } else {
            Item item = GsonHelper.getAsItem(pJson, "item");
            if (pJson.has("data")) {
                throw new JsonParseException("Disallowed data tag found");
            } else {
                ItemStack itemstack = new ItemStack(item);
                if (pJson.has("nbt")) {
                    try {
                        CompoundTag compoundtag = TagParser.parseTag(GsonHelper.convertToString(pJson.get("nbt"), "nbt"));
                        itemstack.setTag(compoundtag);
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
                    }
                }

                return itemstack;
            }
        }
    }

    public static JsonObject itemToJson(ItemStack item) {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", ForgeRegistries.ITEMS.getKey(item.getItem()).toString());
        if (item.hasTag()) {
            jsonobject.addProperty("nbt", item.getTag().toString());
        }

        return jsonobject;
    }

    public static JsonObject effectInstanceToJson(MobEffectInstance effect){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("effect", MobEffect.getId(effect.getEffect())); //TODO: Better by id!!
        jsonObject.addProperty("duration", effect.getDuration());
        jsonObject.addProperty("amplifier", effect.getAmplifier());
        jsonObject.addProperty("ambient", effect.isAmbient());
        jsonObject.addProperty("visible", effect.isVisible());
        jsonObject.addProperty("showIcon", effect.showIcon());

        return jsonObject;
    }

    public static MobEffectInstance effectInstanceFromJson(JsonObject jsonObject){
        MobEffect effect1;
        int duration, amplifier;
        boolean ambient, visible, showIcon;

        effect1 = MobEffect.byId(jsonObject.get("effect").getAsInt());
        duration = jsonObject.get("duration").getAsInt();
        amplifier = jsonObject.get("amplifier").getAsInt();
        ambient = jsonObject.get("ambient").getAsBoolean();
        visible = jsonObject.get("visible").getAsBoolean();
        showIcon = jsonObject.get("showIcon").getAsBoolean();


        return new MobEffectInstance(effect1, duration, amplifier, ambient, visible, showIcon);
    }
}
