package de.thedead2.progression_reloaded.data.trigger;

import de.thedead2.progression_reloaded.data.criteria.ICriterion;
import de.thedead2.progression_reloaded.player.SinglePlayer;

public class SleepTrigger extends SimpleTrigger implements ICriterion {
    @Override
    public SimpleTrigger getTrigger() {
        return this;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, s -> true);
    }
}
