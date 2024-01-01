package de.thedead2.progression_reloaded.items.custom;

import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
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

import static de.thedead2.progression_reloaded.util.helper.MathHelper.secondsToTicks;


public class ExtraLifeItem extends Item {

    public ExtraLifeItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }


    private static boolean unlimited = false;


    public static boolean unlimited() {
        unlimited = !unlimited;
        return unlimited;
    }


    public static boolean isUnlimited() {
        return unlimited;
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if(!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack stack = serverPlayer.getItemInHand(pUsedHand);
            if(rewardExtraLife(serverPlayer, false)) {
                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
            else {
                return InteractionResultHolder.fail(stack);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }


    public static boolean rewardExtraLife(ServerPlayer serverPlayer, boolean isCommand) {
        PlayerData playerData = PlayerDataManager.getPlayerData(serverPlayer);

        if((!isCommand && serverPlayer.isCreative()) || !playerData.addExtraLife()) {
            return false;
        }
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(15), 1));
        serverPlayer.level.playSound(null, new BlockPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1F, 1F);
        PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(playerData), serverPlayer);
        return true;
    }
}
