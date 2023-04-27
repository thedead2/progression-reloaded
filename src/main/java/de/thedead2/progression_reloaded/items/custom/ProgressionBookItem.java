package de.thedead2.progression_reloaded.items.custom;

import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packages.ClientOpenProgressionBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class ProgressionBookItem extends Item {
    public ProgressionBookItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if(!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer){
            ModNetworkHandler.sendToPlayer(new ClientOpenProgressionBookPacket(), serverPlayer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
