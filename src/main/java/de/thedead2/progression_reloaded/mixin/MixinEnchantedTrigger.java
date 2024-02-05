package de.thedead2.progression_reloaded.mixin;

import de.thedead2.progression_reloaded.data.trigger.ItemEnchantedCriterionTrigger;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EnchantedItemTrigger.class)
public class MixinEnchantedTrigger {

    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V")
    public void onItemEnchanted(ServerPlayer pPlayer, ItemStack pItem, int pLevelsSpent, CallbackInfo ci) {
        ItemEnchantedCriterionTrigger.onItemEnchanted(pPlayer, pItem, pLevelsSpent);
    }
}
