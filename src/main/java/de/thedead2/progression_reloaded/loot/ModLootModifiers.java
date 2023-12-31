package de.thedead2.progression_reloaded.loot;

import com.mojang.serialization.Codec;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ModHelper.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEMS =
            LOOT_MODIFIER_SERIALIZERS.register("add_items", AddItemsModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> REMOVE_ITEMS =
            LOOT_MODIFIER_SERIALIZERS.register("remove_items", RemoveItemsModifier.CODEC);


    public static void register(IEventBus bus) {
        LOOT_MODIFIER_SERIALIZERS.register(bus);
    }

}
