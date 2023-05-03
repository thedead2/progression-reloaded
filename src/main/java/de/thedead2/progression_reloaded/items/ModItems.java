package de.thedead2.progression_reloaded.items;

import de.thedead2.progression_reloaded.items.custom.ProgressionBookItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> PROGRESSION_BOOK = ITEMS.register("progression_book", ProgressionBookItem::new);
    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
