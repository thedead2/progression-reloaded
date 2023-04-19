package de.thedead2.progression_reloaded.gui.editors.insert;

import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.handlers.APIHandler;
import de.thedead2.progression_reloaded.handlers.RuleHandler;

import java.util.Collection;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CRITERIA_EDITOR;

public class FeatureNewTrigger extends FeatureNew<ITriggerProvider> {

    public FeatureNewTrigger() {
        super("trigger");
    }

    @Override
    public Collection<ITriggerProvider> getFields() {
        return APIHandler.triggerTypes.values();
    }

    @Override
    public void clone(ITriggerProvider trigger) {
        RuleHandler.cloneTrigger(CRITERIA_EDITOR.get(), trigger);
    }
}
