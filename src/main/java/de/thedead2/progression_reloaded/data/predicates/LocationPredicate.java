package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


public class LocationPredicate implements ITriggerPredicate<BlockPos> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("location");

    public static final LocationPredicate ANY = new LocationPredicate(MinMax.Doubles.ANY, MinMax.Doubles.ANY, MinMax.Doubles.ANY, null, null, null);

    private final MinMax.Doubles x;

    private final MinMax.Doubles y;

    private final MinMax.Doubles z;

    @Nullable
    private final ResourceKey<Biome> biome;

    @Nullable
    private final ResourceKey<Structure> structure;

    @Nullable
    private final ResourceKey<Level> dimension;


    public LocationPredicate(MinMax.Doubles x, MinMax.Doubles y, MinMax.Doubles z, @Nullable ResourceKey<Biome> biome, @Nullable ResourceKey<Structure> structure, @Nullable ResourceKey<Level> dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.biome = biome;
        this.structure = structure;
        this.dimension = dimension;
    }


    public static LocationPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            JsonObject position = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());
            MinMax.Doubles x = MinMax.Doubles.fromJson(position.get("x"));
            MinMax.Doubles y = MinMax.Doubles.fromJson(position.get("y"));
            MinMax.Doubles z = MinMax.Doubles.fromJson(position.get("z"));

            ResourceKey<Level> levelKey = SerializationHelper.getNullable(jsonObject, "dimension", jsonElement1 -> ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonElement1)
                                                                                                                                         .resultOrPartial(LOGGER::error)
                                                                                                                                         .map((resourceLocation) -> ResourceKey.create(Registries.DIMENSION, resourceLocation))
                                                                                                                                         .orElse(null));
            ResourceKey<Structure> structureKey = SerializationHelper.getNullable(jsonObject, "structure", jsonElement1 -> ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonElement1)
                                                                                                                                                 .resultOrPartial(LOGGER::error)
                                                                                                                                                 .map((resourceLocation) -> ResourceKey.create(Registries.STRUCTURE, resourceLocation))
                                                                                                                                                 .orElse(null));
            ResourceKey<Biome> biomeKey = SerializationHelper.getNullable(jsonObject, "biome", jsonElement1 -> {
                ResourceLocation resourceLocation = new ResourceLocation(jsonElement1.getAsString());
                return ResourceKey.create(Registries.BIOME, resourceLocation);
            });

            return new LocationPredicate(x, y, z, biomeKey, structureKey, levelKey);
        }
        else {
            return ANY;
        }
    }


    @Override
    public boolean matches(BlockPos blockPos, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        ServerLevel serverLevel = addArgs[0] != null ? (ServerLevel) addArgs[0] : null;
        if(!this.x.matches((double) blockPos.getX())) {
            return false;
        }
        else if(!this.y.matches((double) blockPos.getY())) {
            return false;
        }
        else if(!this.z.matches((double) blockPos.getZ())) {
            return false;
        }
        else if(serverLevel == null) {
            return true;
        }
        else if(this.dimension != null && this.dimension != serverLevel.dimension()) {
            return false;
        }
        else {
            boolean flag = serverLevel.isLoaded(blockPos);
            if(this.biome == null || flag && serverLevel.getBiome(blockPos).is(this.biome)) {
                return this.structure == null || flag && serverLevel.structureManager().getStructureWithPieceAt(blockPos, this.structure).isValid();
            }
            else {
                return false;
            }
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonObject = new JsonObject();
            if(!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
                JsonObject position = new JsonObject();
                position.add("x", this.x.toJson());
                position.add("y", this.y.toJson());
                position.add("z", this.z.toJson());
                jsonObject.add("position", position);
            }

            SerializationHelper.addNullable(this.dimension, jsonObject, "dimension", dimension -> Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, dimension).resultOrPartial(LOGGER::fatal).orElseThrow());
            SerializationHelper.addNullable(this.structure, jsonObject, "structure", structureKey -> new JsonPrimitive(structureKey.location().toString()));
            SerializationHelper.addNullable(this.biome, jsonObject, "biome", biomeKey -> new JsonPrimitive(biomeKey.location().toString()));

            return jsonObject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" anywhere");
        }
        else {
            if(!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
                return Component.literal(" at position ").append(this.x.getDefaultDescription()).append(", ").append(this.y.getDefaultDescription()).append(", ").append(this.z.getDefaultDescription());
            }
            else if(this.dimension != null) {
                return Component.literal(" in the ").append(this.dimension.location().getPath());
            }
            else if(this.biome != null) {
                return Component.literal(" in the ").append(this.biome.location().getPath());
            }
            else {
                return Component.literal(" in a ").append(this.structure.location().getPath());
            }
        }
    }

}
