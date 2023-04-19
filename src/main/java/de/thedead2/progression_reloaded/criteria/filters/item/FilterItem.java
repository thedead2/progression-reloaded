package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IItemGetterCallback;
import de.thedead2.progression_reloaded.api.special.ISetterCallback;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

@ProgressionRule(name="itemOnly", color=0xFFCCCCCC)
public class FilterItem extends FilterBaseItem implements IItemGetterCallback, ISetterCallback, ISpecialFieldProvider {
    public Item item = Items.BEEF;

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItem(this, "item", 25, 25, 3F));
    }

    @Override
    public boolean matches(ItemStack check) {
        return item == check.getItem();
    }

    @Override
    public ItemStack getItem(String fieldName) {
        return new ItemStack(item);
    }

    @Override
    public boolean setField(String fieldName, Object stack) {
        item = ((ItemStack) stack).getItem();
        return true;
    }
}
