package de.thedead2.progression_reloaded.datagen.dataprovider;

import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.loot.AddItemsModifier;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.List;


public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {

    private static final float extraLifeProbability = 0.075f;

    private static final float halfExtraLifeProbability = extraLifeProbability * 2;

    private static final float quarterExtraLifeProbability = extraLifeProbability * 4;

    private static final List<String> chestIds = List.of("chests/abandoned_mineshaft", "chests/ancient_city", "chests/bastion_treasure", "chests/buried_treasure",
                                                         "chests/end_city_treasure", "chests/jungle_temple", "chests/pillager_outpost", "chests/ruined_portal",
                                                         "chests/shipwreck_treasure", "chests/stronghold_library", "chests/woodland_mansion"
    );

    private static final List<String> entityIds = List.of("entities/elder_guardian", "entities/ender_dragon", "entities/wither", "entities/evoker", "entities/illusioner", "entities/ravager",
                                                          "entities/vindicator", "entities/warden"
    );

    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, ModHelper.MOD_ID);
    }


    @Override
    protected void start() {
        addItem("extra_life", ModItems.EXTRA_LIFE.get(), extraLifeProbability, chestIds);
        addItem("extra_life", ModItems.EXTRA_LIFE.get(), extraLifeProbability * 4, entityIds);
        addItem("half_extra_life", ModItems.HALF_EXTRA_LIFE.get(), halfExtraLifeProbability, chestIds);
        addItem("half_extra_life", ModItems.HALF_EXTRA_LIFE.get(), halfExtraLifeProbability * 3, entityIds);
        addItem("quarter_extra_life", ModItems.QUARTER_EXTRA_LIFE.get(), quarterExtraLifeProbability, chestIds);
        addItem("quarter_extra_life", ModItems.QUARTER_EXTRA_LIFE.get(), quarterExtraLifeProbability * 2, entityIds);
    }


    private void addItem(String name, Item item, float probability, List<String> ids) {
        for(String id : ids) {
            add(name + "_from_" + id.substring(id.indexOf("/") + 1), new AddItemsModifier(new LootItemCondition[]{
                    new LootTableIdCondition.Builder(new ResourceLocation(id)).build(),
                    LootItemRandomChanceCondition.randomChance(probability).build()
            }, item));
        }
    }
}
