package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import net.minecraft.entity.player.EntityPlayer;

@ProgressionRule(name="timeofday", color=0xFFF99100, icon="minecraft:clock")
public class ConditionTime extends ConditionBase implements ICustomDescription {
    public int timeMin = 0;
    public int timeMax = 0;

    @Override
    public String getDescription() {
        if (getProvider().isInverted()) return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description.inverted", timeMin, timeMax);
        else return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", timeMin, timeMax);
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) {
            long time = player.worldObj.getWorldTime() % 24000L;
            if (time >= timeMin && time <= timeMax) return true;
        }

        return false;
    }
}
