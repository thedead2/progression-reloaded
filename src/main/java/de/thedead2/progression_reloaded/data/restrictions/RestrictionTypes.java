package de.thedead2.progression_reloaded.data.restrictions;

import de.thedead2.progression_reloaded.api.IRestrictionType;
import de.thedead2.progression_reloaded.data.RestrictionManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;
import java.util.stream.Stream;

import static de.thedead2.progression_reloaded.api.IRestrictionType.getFromRegistry;


public class RestrictionTypes {

    public static final IRestrictionType<Item> ITEM = register("item", ForgeRegistries.ITEMS);

    public static final IRestrictionType<Block> BLOCK = register("block", ForgeRegistries.BLOCKS);

    public static final IRestrictionType<EntityType<?>> ENTITY = register("entity", ForgeRegistries.ENTITY_TYPES);

    public static final IRestrictionType<ResourceKey<Level>> DIMENSION = register("dimension", dimensionKey -> Pair.of(dimensionKey.location(), Stream.empty()));


    public static <T> IRestrictionType<T> register(String name, IForgeRegistry<T> registry) {
        return register(name, t -> getFromRegistry(registry, t));
    }


    public static <T> IRestrictionType<T> register(String name, Function<T, Pair<ResourceLocation, Stream<TagKey<T>>>> getter) {
        IRestrictionType<T> restrictionType = getter::apply;

        RestrictionManager.RESTRICTION_TYPES.put(new ResourceLocation(ModHelper.MOD_ID, "restriction_types_" + name), restrictionType);

        return restrictionType;
    }
}
