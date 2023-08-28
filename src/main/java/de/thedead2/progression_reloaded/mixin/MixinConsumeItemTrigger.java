package de.thedead2.progression_reloaded.mixin;

import de.thedead2.progression_reloaded.data.trigger.PlayerConsumedItemTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ConsumeItemTrigger.class)
public class MixinConsumeItemTrigger {

    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V")
    public void onItemConsumed(ServerPlayer pPlayer, ItemStack pItem, CallbackInfo ci) {
        PlayerConsumedItemTrigger.onItemConsumed(pPlayer, pItem);
    }
}
