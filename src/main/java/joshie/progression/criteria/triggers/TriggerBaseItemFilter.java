package joshie.progression.criteria.triggers;

import joshie.progression.api.criteria.IProgressionFilter;
import joshie.progression.api.special.IHasFilters;
import joshie.progression.helpers.ItemHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class TriggerBaseItemFilter extends TriggerBaseCounter implements IHasFilters {
    public List<IProgressionFilter> filters = new ArrayList();
    protected ItemStack BROKEN;
    protected ItemStack preview;
    protected int ticker;

    public TriggerBaseItemFilter(String name, int color) {
        super(name, color);
    }

    public TriggerBaseItemFilter(String name, int color, String data) {
        super(name, color, data);
    }

    public TriggerBaseItemFilter copyFilter(TriggerBaseItemFilter trigger) {
        trigger.filters = filters;
        return trigger;
    }
    
    @Override
    public List<IProgressionFilter> getAllFilters() {
        return filters;
    }
    
    @Override
    public ItemStack getIcon() {
        if (ticker == 0 || ticker >= 200) {
            preview = ItemHelper.getRandomItem(filters);
            ticker = 1;
        }
        
        ticker++;
        
        return preview == null ? BROKEN: preview;
    }
}
