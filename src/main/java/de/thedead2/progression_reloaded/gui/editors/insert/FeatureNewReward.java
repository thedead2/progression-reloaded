package de.thedead2.progression_reloaded.gui.editors.insert;

import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.handlers.APIHandler;
import de.thedead2.progression_reloaded.handlers.RuleHandler;

import java.util.Collection;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CRITERIA_EDITOR;

public class FeatureNewReward extends FeatureNew<IRewardProvider> {

    public FeatureNewReward() {
        super("reward");
    }

    @Override
    public Collection<IRewardProvider> getFields() {
        return APIHandler.rewardTypes.values();
    }

    @Override
    public void clone(IRewardProvider reward) {
        RuleHandler.cloneReward(CRITERIA_EDITOR.get(), reward);
    }
}
