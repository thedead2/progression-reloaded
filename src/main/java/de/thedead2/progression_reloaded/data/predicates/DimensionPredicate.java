package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Map;

public class DimensionPredicate implements ITriggerPredicate<DimensionType> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("dimension");
    @Override
    public boolean matches(DimensionType dimensionType, Object... addArgs) {
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
    public Builder<DimensionPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<DimensionType> copy() {
        return null;
    }

    public static DimensionPredicate from(DimensionType dimensionType) {
        return null;
    }
}
