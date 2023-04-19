package de.thedead2.progression_reloaded.gui.filters;

import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;

import java.util.List;

public class FilterTypePotion extends FilterTypeItem {
    public static final IFilterType INSTANCE = new FilterTypePotion();

    @Override
    public String getName() {
        return "potioneffect";
    }
    
    @Override
    public List<ItemStack> getAllItems() {
        return ItemHelper.getAllItems();
    }

    @Override
    public boolean isAcceptedItem(ItemStack stack) {
        return PotionUtils.getEffectsFromStack(stack).size() > 0;
    }
}
