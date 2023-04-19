package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;

import java.util.List;

import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class FeatureFilter extends FeatureDrawable<IFilterProvider> {
    public FeatureFilter() {
        super("filter", 45, NEW_FILTER, THEME.conditionGradient1, THEME.conditionGradient2, THEME.conditionFontColor, 0xFFF9F462);
    }

    @Override
    public boolean isReady() {
        return FILTER_EDITOR.get() != null;
    }
    
    @Override
    public List<IFilterProvider> getList() {
        return FILTER_EDITOR.get().getFilters();
    }
}
