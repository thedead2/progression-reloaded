package de.thedead2.progression_reloaded.criteria.filters.location;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IFilter;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.lib.WorldLocation;

public abstract class FilterLocationBase implements IFilter<WorldLocation, WorldLocation> {
    private IFilterProvider provider;

    @Override
    public IFilterProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(IFilterProvider provider) {
        this.provider = provider;
    }

    @Override
    public void apply(WorldLocation location) {}

    @Override
    public boolean matches(Object object) {
        if (!(object instanceof WorldLocation)) return false;
        WorldLocation location = ((WorldLocation)object);
        if (location.player == null) return false;
        return matches(location);
    }

    @Override
    public IFilterType getType() {
        return ProgressionAPI.filters.getLocationFilter();
    }

    public boolean matches(WorldLocation location) {
        return location.equals(getRandom(location.player));
    }

    public enum LocationOperator {
        THISORMORE, THISORLESS, RADIUS;
    }
}