package de.thedead2.progression_reloaded.items;

import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.items.custom.LootChestItem;
import de.thedead2.progression_reloaded.items.custom.ProgressionBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> PROGRESSION_BOOK = ITEMS.register("progression_book", ProgressionBookItem::new);

    public static final RegistryObject<Item> EXTRA_LIFE = ITEMS.register("extra_life", ExtraLifeItem::new);

    public static final RegistryObject<Item> HALF_EXTRA_LIFE = ITEMS.register("half_extra_life", () -> new Item(new Item.Properties().stacksTo(2).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> QUARTER_EXTRA_LIFE = ITEMS.register("quarter_extra_life", () -> new Item(new Item.Properties().stacksTo(4).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> LOOT_CHEST = ITEMS.register("loot_chest", LootChestItem::new);
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
