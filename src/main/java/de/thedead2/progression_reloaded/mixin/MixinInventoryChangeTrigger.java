package de.thedead2.progression_reloaded.mixin;

import de.thedead2.progression_reloaded.data.trigger.PlayerInventoryChangedCriterionTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(InventoryChangeTrigger.class)
public class MixinInventoryChangeTrigger {

    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V")
    public void onInventoryChanged(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack, CallbackInfo ci) {
        PlayerInventoryChangedCriterionTrigger.onInventoryChanged(pPlayer, pStack);
    }
}
