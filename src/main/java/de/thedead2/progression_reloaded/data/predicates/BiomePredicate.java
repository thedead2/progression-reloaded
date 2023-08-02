package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

public class BiomePredicate implements ITriggerPredicate<Biome> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("biome");
    @Override
    public boolean matches(Biome biome, Object... addArgs) {
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
    public Builder<BiomePredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<Biome> copy() {
        return null;
    }
    public static BiomePredicate from(Biome biome) {
        return null;
    }
}
