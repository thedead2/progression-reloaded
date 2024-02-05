package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;


public class EnchantmentPredicate implements ITriggerPredicate<Map<Enchantment, Integer>> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("enchantment");

    public static final EnchantmentPredicate ANY = new EnchantmentPredicate(null, MinMax.Ints.ANY);

    @Nullable
    private final Enchantment enchantment;

    private final MinMax.Ints level;


    public EnchantmentPredicate(@Nullable Enchantment enchantment, MinMax.Ints level) {
        this.enchantment = enchantment;
        this.level = level;
    }


    public static EnchantmentPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();
            Enchantment enchantment = SerializationHelper.getNullable(jsonobject, "enchantment", jsonElement1 -> {
                ResourceLocation resourcelocation = new ResourceLocation(jsonElement1.getAsString());
                return ForgeRegistries.ENCHANTMENTS.getValue(resourcelocation);
            });

            MinMax.Ints level = MinMax.Ints.fromJson(jsonobject.get("level"));

            return new EnchantmentPredicate(enchantment, level);
        }
        else {
            return ANY;
        }
    }


    @Override
    public boolean matches(Map<Enchantment, Integer> enchantments, Object... addArgs) {
        if(this.enchantment != null) {
            if(!enchantments.containsKey(this.enchantment)) {
                return false;
            }

            int i = enchantments.get(this.enchantment);
            return this.level == MinMax.Ints.ANY || this.level.matches(i);
        }
        else if(this.level != MinMax.Ints.ANY) {
            for(Integer integer : enchantments.values()) {
                if(this.level.matches(integer)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            SerializationHelper.addNullable(this.enchantment, jsonobject, "enchantment", enchantment1 -> new JsonPrimitive(ForgeRegistries.ENCHANTMENTS.getKey(enchantment1).toString()));
            jsonobject.add("level", this.level.toJson());
            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" anything");
        }
        else {
            MutableComponent component = Component.literal(" ").append(Component.translatable(this.enchantment.getDescriptionId()));
            if(!this.level.isAny()) {
                component.append(" with level ").append(this.level.getDefaultDescription());
            }

            return component;
        }
    }
}
