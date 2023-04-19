package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.entity.player.EntityPlayer;

@ProgressionRule(name="daytime", color=0xFFFFFF00, meta="ifDayOrNight")
public class ConditionDaytime extends ConditionBase {
    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) {
            if (player.worldObj.isDaytime()) return true;
        }

        return false;
    }
}
