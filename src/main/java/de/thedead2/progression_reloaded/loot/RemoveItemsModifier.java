package de.thedead2.progression_reloaded.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;


public class RemoveItemsModifier extends LootModifier {

    public static final Supplier<Codec<RemoveItemsModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> codecStart(instance).and(ForgeRegistries.ITEMS.getCodec().listOf().fieldOf("items").forGetter(m -> m.items))
                                                                                                                                                       .apply(instance, RemoveItemsModifier::new)));

    private final List<Item> items;


    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    public RemoveItemsModifier(LootItemCondition[] conditionsIn, Item... items) {
        this(conditionsIn, List.of(items));
    }


    public RemoveItemsModifier(LootItemCondition[] conditionsIn, List<Item> items) {
        super(conditionsIn);
        this.items = items;
    }


    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for(LootItemCondition condition : this.conditions) {
            if(!condition.test(context)) {
                return generatedLoot;
            }
        }
        for(Item item : this.items) {
            generatedLoot.remove(new ItemStack(item));
        }

        return generatedLoot;
    }


    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
