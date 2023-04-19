package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.item.ItemStack;

@ProgressionRule(name="metadata", color=0xFFFF73FF)
public class FilterItemMeta extends FilterBaseItem {
    public int damage = 0;

    @Override
    public boolean matches(ItemStack check) {
        return check.getItemDamage() == damage || damage == Short.MAX_VALUE;
    }
}
