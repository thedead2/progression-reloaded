package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ICustomWidth;

public abstract class RewardBaseSingular extends RewardBase implements ICustomDescription, ICustomWidth {
    @Override
    public int getWidth(DisplayMode mode) {
        return 100;
    }

    @Override
    public boolean shouldRunOnce() {
        return true;
    }
}
