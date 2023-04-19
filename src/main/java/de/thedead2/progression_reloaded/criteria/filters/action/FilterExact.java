package de.thedead2.progression_reloaded.criteria.filters.action;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ICustomWidth;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.crafting.ActionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

@ProgressionRule(name="exact", color=0xFF663300)
public class FilterExact extends FilterBaseAction implements ICustomWidth, ICustomDescription, ISpecialFieldProvider {
    public ItemStack stack = new ItemStack(Blocks.CRAFTING_TABLE);

    @Override
    public int getWidth(DisplayMode mode) {
        return 55;
    }

    @Override
    public String getDescription() {
        return ActionType.getCraftingActionFromIcon(stack).getDisplayName();
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItem(this, "stack", 5, 25, 2.8F));
    }
    
    @Override
    public ItemStack getRandom(EntityPlayer player) {
        return stack;
    }

    @Override
    public boolean matches(ItemStack check) {
        if (stack.getItem() != check.getItem()) return false;
        if ((stack.getItemDamage() != check.getItemDamage())) return false;
        return true;
    }
}
