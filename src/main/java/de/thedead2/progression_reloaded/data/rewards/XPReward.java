package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.world.entity.player.Player;

public class XPReward implements IReward{

    private final int amount;

    public XPReward(int amount) {
        this.amount = amount;
    }

    @Override
    public void rewardPlayer(Player player) {
        player.giveExperiencePoints(amount);
    }
}
