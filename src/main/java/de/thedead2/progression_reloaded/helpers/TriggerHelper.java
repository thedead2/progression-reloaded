package de.thedead2.progression_reloaded.helpers;

import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;

public class TriggerHelper {
    public static int getInternalID(ITriggerProvider trigger) {
        for (int id = 0; id < trigger.getCriteria().getTriggers().size(); id++) {
            ITriggerProvider aTrigger = trigger.getCriteria().getTriggers().get(id);
            if (aTrigger.equals(trigger)) return id;
        }

        return 0;
    }
}
