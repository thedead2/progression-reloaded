package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;
import java.util.Set;

public class EnchantmentPredicate implements ITriggerPredicate<EnchantmentPredicate>{
    public static final ResourceLocation ID = ITriggerPredicate.createId("enchantment");
    public static final EnchantmentPredicate ANY = new EnchantmentPredicate();

    @Override
    public boolean matches(EnchantmentPredicate enchantmentPredicate, Object... addArgs) {
        return false;
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public Builder<? extends ITriggerPredicate<EnchantmentPredicate>> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<EnchantmentPredicate> copy() {
        return null;
    }

    public static EnchantmentPredicate from(Enchantment enchantment) {
        return null;
    }

    public static EnchantmentPredicate fromSet(Set<Enchantment> enchantments){
        EnchantmentPredicate enchantmentPredicates = new EnchantmentPredicate();

        return enchantmentPredicates;
    }
}
