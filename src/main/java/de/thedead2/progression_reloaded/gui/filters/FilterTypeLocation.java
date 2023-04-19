package de.thedead2.progression_reloaded.gui.filters;

import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.lib.WorldLocation;

import java.util.ArrayList;
import java.util.List;

public class FilterTypeLocation extends FilterTypeBase {
    public static final IFilterType INSTANCE = new FilterTypeLocation();

    @Override
    public String getName() {
        return "location";
    }

    @Override
    public boolean isAcceptable(Object object) {
        return object instanceof WorldLocation;
    }

    @Override
    public List<WorldLocation> getAllItems() {
        return new ArrayList();
    }
}
