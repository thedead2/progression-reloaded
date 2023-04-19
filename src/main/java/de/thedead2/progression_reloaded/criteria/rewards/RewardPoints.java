package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import net.minecraft.entity.player.EntityPlayerMP;

@ProgressionRule(name="points", color=0xFF002DB2, meta="points")
public class RewardPoints extends RewardBaseSingular {
    public String variable = "gold";
    public int amount = 1;

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.points.description", amount, variable);
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.DISPLAY ? 75: 100;
    }

    @Override
    public void reward(EntityPlayerMP player) {
        ProgressionAPI.player.addDouble(player, variable, amount);
    }
}
