package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.special.ICustomIcon;
import de.thedead2.progression_reloaded.api.special.IHasFilters;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class TriggerBaseItemFilter extends TriggerBaseCounter implements IHasFilters, ICustomIcon {
    public List<IFilterProvider> filters = new ArrayList();
    protected ItemStack BROKEN;
    protected ItemStack preview;
    protected int ticker;

    public TriggerBaseItemFilter copyFilter(TriggerBaseItemFilter trigger) {
        trigger.filters = filters;
        return trigger;
    }
    
    @Override
    public List<IFilterProvider> getAllFilters() {
        return filters;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        return ProgressionAPI.filters.getItemStackFilter();
    }
    
    @Override
    public ItemStack getIcon() {
        if (ticker == 0 || ticker >= 200) {
            preview = ItemHelper.getRandomItemFromFilters(filters, MCClientHelper.getPlayer());
            ticker = 1;
        }
        
        ticker++;
        
        return preview == null ? BROKEN: preview;
    }
}
