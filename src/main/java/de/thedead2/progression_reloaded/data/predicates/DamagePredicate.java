package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Map;

public class DamagePredicate implements ITriggerPredicate<DamageSource> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("damage");

    public static DamagePredicate fromJson(JsonElement dealtDamage) {
        return null;
    }

    @Override
    public boolean matches(DamageSource damageSource, Object... addArgs) {
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
    public Builder<DamagePredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<DamageSource> copy() {
        return null;
    }

    public static DamagePredicate from(DamageSource damageSource) {
        return null;
    }

}
