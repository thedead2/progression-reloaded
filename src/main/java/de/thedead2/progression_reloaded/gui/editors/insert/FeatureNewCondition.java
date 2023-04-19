package de.thedead2.progression_reloaded.gui.editors.insert;

import de.thedead2.progression_reloaded.api.criteria.IConditionProvider;
import de.thedead2.progression_reloaded.handlers.APIHandler;
import de.thedead2.progression_reloaded.handlers.RuleHandler;

import java.util.Collection;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CONDITION_EDITOR;

public class FeatureNewCondition extends FeatureNew<IConditionProvider> {

    public FeatureNewCondition() {
        super("condition");
    }

    @Override
    public Collection<IConditionProvider> getFields() {
        return APIHandler.conditionTypes.values();
    }

    @Override
    public int getColor() {
        return CONDITION_EDITOR.get().getColor();
    }

    @Override
    public void clone(IConditionProvider provider) {
        RuleHandler.cloneCondition(CONDITION_EDITOR.get(), provider);
    }
}
