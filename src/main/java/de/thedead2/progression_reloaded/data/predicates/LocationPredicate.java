package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.Map;

public class LocationPredicate implements ITriggerPredicate<BlockPos> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("location");

    @SuppressWarnings("unchecked")
    public static final LocationPredicate ANY = new LocationPredicate((MinMaxNumber<Double>) MinMaxNumber.ANY, (MinMaxNumber<Double>) MinMaxNumber.ANY, (MinMaxNumber<Double>) MinMaxNumber.ANY, null, null, null);
    private final MinMaxNumber<Double> x;
    private final MinMaxNumber<Double> y;
    private final MinMaxNumber<Double> z;
    @Nullable
    private final ResourceKey<Biome> biome;
    @Nullable
    private final ResourceKey<Structure> structure;
    @Nullable
    private final ResourceKey<Level> dimension;

    public LocationPredicate(MinMaxNumber<Double> x, MinMaxNumber<Double> y, MinMaxNumber<Double> z, @Nullable ResourceKey<Biome> biome, @Nullable ResourceKey<Structure> structure, @Nullable ResourceKey<Level> dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.biome = biome;
        this.structure = structure;
        this.dimension = dimension;
    }

    public static LocationPredicate fromJson(JsonElement location) {
        return null;
    }

    @Override
    public boolean matches(BlockPos blockPos, Object... addArgs) {
        if(this == ANY) return true;

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
    public Builder<LocationPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<BlockPos> copy() {
        return null;
    }


    public static LocationPredicate from(BlockPos blockPos) {
        return null;
    }

}
