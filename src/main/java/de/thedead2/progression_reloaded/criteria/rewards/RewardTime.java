package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import net.minecraft.entity.player.EntityPlayerMP;

@ProgressionRule(name="time", color=0xFF26C9FF, icon="minecraft:clock")
public class RewardTime extends RewardBaseSingular {
    public boolean addTime = false;
    public int time = 0;

    @Override
    public String getDescription() {
        if (addTime) return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.time.add", time);
        else return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.time.set", time);
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.EDIT ? 100 : 55;
    }

    @Override
    public void reward(EntityPlayerMP player) {
        if (addTime) {
            player.worldObj.setWorldTime(player.worldObj.getWorldTime() + (long) time);
        } else player.worldObj.setWorldTime(time);
    }
}
