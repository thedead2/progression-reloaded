package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.item.ItemStack;

@ProgressionRule(name="toolType", color=0xFF2B2B2B)
public class FilterItemTool extends FilterBaseItem {
    public String tooltype = "pickaxe";

    @Override
    public boolean matches(ItemStack check) {
        return check.getItem().getToolClasses(check).contains(tooltype);
    }
}