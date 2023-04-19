package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ICustomWidth;

@ProgressionRule(name="boolean", color=0xFF00FFBF, meta="ifHasBoolean")
public class ConditionBoolean extends ConditionBase implements ICustomDescription, ICustomWidth {
    public String variable = "default";
    public String description = "Has done something.";
    public int displayWidth = 85;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.DISPLAY ? displayWidth : 100;
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        return ProgressionAPI.player.getBoolean(team.getOwner(), variable, false) == !getProvider().isInverted();
    }
}
