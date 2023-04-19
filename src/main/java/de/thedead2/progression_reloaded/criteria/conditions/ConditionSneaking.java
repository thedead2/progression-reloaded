package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.entity.player.EntityPlayer;

@ProgressionRule(name="isSneaking", color=0xFFD96D00, meta="isSneaking")
public class ConditionSneaking extends ConditionBase {
    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) {
            return player.isSneaking();
        }

        return false;
    }
}