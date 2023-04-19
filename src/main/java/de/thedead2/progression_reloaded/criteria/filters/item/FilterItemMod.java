package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.helpers.StackHelper;
import net.minecraft.item.ItemStack;

@ProgressionRule(name="modid", color=0xFFFF8000)
public class FilterItemMod extends FilterBaseItem {
    public String modid = "minecraft";

    @Override
    public boolean matches(ItemStack check) {
        if (modid.equals("*")) return true;
        return StackHelper.getModFromItem(check.getItem()).equals(modid);
    }
}
