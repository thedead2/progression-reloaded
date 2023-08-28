package de.thedead2.progression_reloaded.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AdvancementsScreen.class)
public class MixinAdvancementsScreen {

    @Inject(at = @At("HEAD"), method = "onUpdateAdvancementProgress", cancellable = true)
    public void onUpdateAdvancementProgress(Advancement advancementIn, AdvancementProgress progress, CallbackInfo ci) {
        if(advancementIn == null) {
            ci.cancel();
        }
    }
}
