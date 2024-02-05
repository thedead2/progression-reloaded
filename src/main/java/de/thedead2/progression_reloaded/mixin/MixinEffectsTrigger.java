package de.thedead2.progression_reloaded.mixin;

import de.thedead2.progression_reloaded.data.trigger.PlayerEffectsChangedCriterionTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EffectsChangedTrigger.class)
public class MixinEffectsTrigger {

    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;)V")
    public void onEffectsChanged(ServerPlayer pPlayer, Entity pSource, CallbackInfo ci) {
        PlayerEffectsChangedCriterionTrigger.onEffectsChanged(pPlayer, pSource);
    }
}
