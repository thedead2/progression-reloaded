package de.thedead2.progression_reloaded.items.custom;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;


public class LootChestItem extends Item {

    public LootChestItem() {
        super(new Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if(!pLevel.isClientSide() && pLevel instanceof ServerLevel serverLevel && pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack stack = serverPlayer.getItemInHand(pUsedHand);
            stack.shrink(1);
            Random random = new Random();
            LootTables lootTables = serverLevel.getServer().getLootTables();
            LootContext lootContext = new LootContext.Builder(serverLevel).withLuck(serverPlayer.getLuck()).create(LootContextParamSet.builder().build());
            List<ResourceLocation> lootTableIds =
                    lootTables.getIds().stream().filter(resourceLocation -> !resourceLocation.getPath().contains("blocks") && !resourceLocation.getPath().contains("fishing")).toList();

            LootTable randomLootTable = lootTables.get(lootTableIds.get(random.nextInt(lootTableIds.size())));
            ObjectArrayList<ItemStack> items = randomLootTable.getRandomItems(lootContext);

            for(ItemStack itemStack : items) {
                if(!serverPlayer.addItem(itemStack)) {
                    serverPlayer.drop(itemStack, true, false); //TODO: Sometimes you have to click multiple times to get loot
                }
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
