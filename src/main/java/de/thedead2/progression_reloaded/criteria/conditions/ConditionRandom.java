package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ICustomWidth;

import java.util.Random;

@ProgressionRule(name="chance", color=0xFF00FFBF, meta="ifRandom")
public class ConditionRandom extends ConditionBase implements ICustomDescription, ICustomWidth {
    private static final Random rand = new Random();
    public double chance = 50D;

    @Override
    public String getDescription() {
        if (getProvider().isInverted()) return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", 100D - chance);
        else return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", chance);
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.DISPLAY ? 65: 100;
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        return (rand.nextDouble() * 100) <= chance;
    }
}
