package de.thedead2.progression_reloaded.gui.editors.insert;

import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.handlers.APIHandler;
import de.thedead2.progression_reloaded.handlers.RuleHandler;

import java.util.Collection;

import static de.thedead2.progression_reloaded.gui.core.GuiList.FILTER_EDITOR;

public class FeatureNewFilter extends FeatureNew<IFilterProvider> {

    public FeatureNewFilter() {
        super("item");
    }

    @Override
    public Collection<IFilterProvider> getFields() {
        return APIHandler.filterTypes.values();
    }

    @Override
    public void clone(IFilterProvider provider) {
        RuleHandler.cloneFilter(FILTER_EDITOR.get(), provider);
        //GuiFilterEditor.GROUP_EDITOR.initGui(); //Refresh the gui
    }

    @Override
    public boolean isValid(IFilterProvider filter) {
        return FILTER_EDITOR.get().isAccepted(filter);
    }
}
