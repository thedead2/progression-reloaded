package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class XPReward implements IReward{

    private final int amount;
    private final boolean levels;

    public XPReward(int amount, boolean levels) {
        this.amount = amount;
        this.levels = levels;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        if (levels) player.giveExperienceLevels(amount);
        else player.giveExperiencePoints(amount);
    }
}
