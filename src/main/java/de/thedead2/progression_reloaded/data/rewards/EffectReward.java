package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectReward implements IReward{
    private final MobEffectInstance effect;

    public EffectReward(MobEffectInstance effect) {
        this.effect = effect;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        player.addEffect(effect);
    }
}
