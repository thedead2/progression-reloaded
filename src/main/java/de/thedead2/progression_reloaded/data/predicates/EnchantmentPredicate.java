package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;

public class EnchantmentPredicate implements ITriggerPredicate<Map<Enchantment, Integer>>{
    public static final ResourceLocation ID = ITriggerPredicate.createId("enchantment");
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate(null, MinMax.Ints.ANY);
    @Nullable
    private final Enchantment enchantment;
    private final MinMax.Ints level;

    public EnchantmentPredicate(@Nullable Enchantment pEnchantment, MinMax.Ints pLevel) {
        this.enchantment = pEnchantment;
        this.level = pLevel;
    }
    public static EnchantmentPredicate fromJson(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonElement, "enchantment");
            Enchantment enchantment = null;
            if (jsonobject.has("enchantment")) {
                ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "enchantment"));
                enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourcelocation);
            }

            MinMax.Ints levels = MinMax.Ints.fromJson(jsonobject.get("levels"));
            return new EnchantmentPredicate(enchantment, levels);
        } else {
            return ANY;
        }
    }

    @Override
    public boolean matches(Map<Enchantment, Integer> enchantments, Object... addArgs) {
        if (this.enchantment != null) {
            if (!enchantments.containsKey(this.enchantment)) {
                return false;
            }

            int i = enchantments.get(this.enchantment);
            if (this.level != MinMax.Ints.ANY && !this.level.matches(i)) {
                return false;
            }
        } else if (this.level != MinMax.Ints.ANY) {
            for(Integer integer : enchantments.values()) {
                if (this.level.matches(integer)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }


    @Override
    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();
            if (this.enchantment != null) {
                jsonobject.addProperty("enchantment", ForgeRegistries.ENCHANTMENTS.getKey(this.enchantment).toString());
            }

            jsonobject.add("levels", this.level.serializeToJson());
            return jsonobject;
        }
    }

    public static EnchantmentPredicate from(Enchantment enchantment) {
        if(enchantment == null) return ANY;
        return new EnchantmentPredicate(enchantment, MinMax.Ints.between(enchantment.getMinLevel(), enchantment.getMaxLevel()));
    }
}
