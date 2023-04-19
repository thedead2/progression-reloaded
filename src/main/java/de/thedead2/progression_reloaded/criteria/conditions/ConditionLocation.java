package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.IHasFilters;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

@ProgressionRule(name="location", color=0xFF111111, meta="ifIsAtCoordinates")
public class ConditionLocation extends ConditionBase implements ICustomDescription, IHasFilters, ISpecialFieldProvider {
    public List<IFilterProvider> locations = new ArrayList();
    protected transient IField field;

    public ConditionLocation() {
        field = new ItemFilterField("locations", this);
    }

    @Override
    public String getDescription() {
        if (getProvider().isInverted()) return de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".description.inverted") + " \n" + field.getField();
        else return de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".description") + " \n" + field.getField();
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) {
            fields.add(new ItemFilterField("locations", this));
        }
    }

    @Override
    public List<IFilterProvider> getAllFilters() {
        return locations;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        return ProgressionAPI.filters.getLocationFilter();
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) {
            WorldLocation location = new WorldLocation(player);
            for (IFilterProvider filter : locations) {
                if (filter.getProvided().matches(location)) return true;
            }
        }

        return  false;
    }
}
