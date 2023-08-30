package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


public class LocationPredicate implements ITriggerPredicate<BlockPos> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("location");

    public static final LocationPredicate ANY = new LocationPredicate(MinMax.Doubles.ANY, MinMax.Doubles.ANY, MinMax.Doubles.ANY, null, null, null, BlockPredicate.ANY, FluidPredicate.ANY);

    private final MinMax.Doubles x;

    private final MinMax.Doubles y;

    private final MinMax.Doubles z;

    @Nullable
    private final ResourceKey<Biome> biome;

    @Nullable
    private final ResourceKey<Structure> structure;

    @Nullable
    private final ResourceKey<Level> dimension;

    private final BlockPredicate block;

    private final FluidPredicate fluid;


    public LocationPredicate(MinMax.Doubles x, MinMax.Doubles y, MinMax.Doubles z, @Nullable ResourceKey<Biome> biome, @Nullable ResourceKey<Structure> structure, @Nullable ResourceKey<Level> dimension, BlockPredicate block, FluidPredicate fluid) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.biome = biome;
        this.structure = structure;
        this.dimension = dimension;
        this.block = block;
        this.fluid = fluid;
    }


    public static LocationPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "location");

            JsonObject jsonObject1 = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());
            MinMax.Doubles x = MinMax.Doubles.fromJson(jsonObject1.get("x"));
            MinMax.Doubles y = MinMax.Doubles.fromJson(jsonObject1.get("y"));
            MinMax.Doubles z = MinMax.Doubles.fromJson(jsonObject1.get("z"));

            ResourceKey<Level> levelKey = jsonObject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension"))
                                                                                              .resultOrPartial(LOGGER::error)
                                                                                              .map((resourceLocation) -> ResourceKey.create(Registries.DIMENSION, resourceLocation))
                                                                                              .orElse(null) : null;
            ResourceKey<Structure> structureKey = jsonObject.has("structure") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("structure"))
                                                                                                      .resultOrPartial(LOGGER::error)
                                                                                                      .map((resourceLocation) -> ResourceKey.create(Registries.STRUCTURE, resourceLocation))
                                                                                                      .orElse(null) : null;
            ResourceKey<Biome> biomeKey = null;
            if(jsonObject.has("biome")) {
                ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "biome"));
                biomeKey = ResourceKey.create(Registries.BIOME, resourcelocation);
            }

            BlockPredicate blockpredicate = BlockPredicate.fromJson(jsonObject.get("block"));
            FluidPredicate fluidpredicate = FluidPredicate.fromJson(jsonObject.get("fluid"));
            return new LocationPredicate(x, y, z, biomeKey, structureKey, levelKey, blockpredicate, fluidpredicate);
        }
        else {
            return ANY;
        }
    }


    public static LocationPredicate from(BlockPos blockPos, Object... addArgs) {
        ServerLevel serverLevel = addArgs[0] != null ? (ServerLevel) addArgs[0] : null;
        double x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
        if(serverLevel != null) {
            ResourceKey<Level> dimensionKey = serverLevel.dimension();
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ForgeRegistries.BIOMES.getKey(serverLevel.getBiome(blockPos).get()));
            AtomicReference<ResourceKey<Structure>> structureKey = new AtomicReference<>();
            serverLevel.structureManager()
                       .getAllStructuresAt(blockPos)
                       .keySet()
                       .stream()
                       .findAny()
                       .ifPresent(structure -> structureKey.set(ResourceKey.create(Registries.STRUCTURE, serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure))));
            BlockPredicate block = BlockPredicate.from(serverLevel.getBlockState(blockPos));
            FluidPredicate fluid = FluidPredicate.from(serverLevel.getFluidState(blockPos));
            return new LocationPredicate(
                    MinMax.Doubles.exactly(x),
                    MinMax.Doubles.exactly(y),
                    MinMax.Doubles.exactly(z),
                    biomeKey,
                    structureKey.get(),
                    dimensionKey,
                    block,
                    fluid
            );
        }
        return new LocationPredicate(
                MinMax.Doubles.exactly(x),
                MinMax.Doubles.exactly(y),
                MinMax.Doubles.exactly(z),
                null,
                null,
                null,
                BlockPredicate.ANY,
                FluidPredicate.ANY
        );
    }


    @Override
    public boolean matches(BlockPos blockPos, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        ServerLevel serverLevel = addArgs[0] != null ? (ServerLevel) addArgs[0] : null;
        if(!this.x.matches(blockPos.getX())) {
            return false;
        }
        else if(!this.y.matches(blockPos.getY())) {
            return false;
        }
        else if(!this.z.matches(blockPos.getZ())) {
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
                if(this.structure == null || flag && serverLevel.structureManager().getStructureWithPieceAt(blockPos, this.structure).isValid()) {
                    if(!this.block.matches(
                            serverLevel.getBlockState(blockPos),
                            serverLevel.getBlockEntity(blockPos)
                    )) {
                        return false;
                    }
                    else {
                        return this.fluid.matches(serverLevel.getFluidState(blockPos));
                    }
                }
                else {
                    return false;
                }
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
                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.add("x", this.x.serializeToJson());
                jsonObject1.add("y", this.y.serializeToJson());
                jsonObject1.add("z", this.z.serializeToJson());
                jsonObject.add("position", jsonObject1);
            }

            if(this.dimension != null) {
                Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension)
                                        .resultOrPartial(LOGGER::fatal)
                                        .ifPresent((jsonElement) -> jsonObject.add("dimension", jsonElement));
            }

            if(this.structure != null) {
                jsonObject.addProperty("structure", this.structure.location().toString());
            }

            if(this.biome != null) {
                jsonObject.addProperty("biome", this.biome.location().toString());
            }

            jsonObject.add("block", this.block.toJson());
            jsonObject.add("fluid", this.fluid.toJson());
            return jsonObject;
        }
    }

}
