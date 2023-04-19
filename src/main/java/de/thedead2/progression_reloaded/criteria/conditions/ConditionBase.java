package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.criteria.ICondition;
import de.thedead2.progression_reloaded.api.criteria.IConditionProvider;

public abstract class ConditionBase implements ICondition {
    private IConditionProvider provider;

    @Override
    public void setProvider(IConditionProvider provider) {
        this.provider = provider;
    }

    @Override
    public IConditionProvider getProvider() {
        return provider;
    }
}
