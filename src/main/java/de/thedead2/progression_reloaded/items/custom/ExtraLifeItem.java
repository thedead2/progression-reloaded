package de.thedead2.progression_reloaded.items.custom;

import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.progression_reloaded.util.ModHelper.secondsToTicks;


public class ExtraLifeItem extends Item {

    public ExtraLifeItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if(!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack stack = serverPlayer.getItemInHand(pUsedHand);
            if(rewardExtraLife(serverPlayer)) {
                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
            else {
                return InteractionResultHolder.fail(stack);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }


    public static boolean rewardExtraLife(ServerPlayer serverPlayer) {
        SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(serverPlayer);

        if(serverPlayer.isCreative() || !singlePlayer.addExtraLife()) {
            return false;
        }
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(15)));
        serverPlayer.level.playSound(null, new BlockPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1F, 1F);
        return true;
    }
}
