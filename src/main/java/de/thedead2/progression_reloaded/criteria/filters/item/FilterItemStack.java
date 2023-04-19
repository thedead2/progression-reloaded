package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.List;

@ProgressionRule(name="itemStack", color=0xFF663300)
public class FilterItemStack extends FilterBaseItem implements ISpecialFieldProvider {
    public ItemStack stack = new ItemStack(Items.CARROT);
    public boolean matchDamage = true;
    public boolean matchNBT = false;

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT)
        fields.add(ProgressionAPI.fields.getItem(this, "stack", 30, 35, 1.4F));
    }

    @Override
    public ItemStack getRandom(EntityPlayer player) {
        return stack.copy();
    }

    @Override
    public boolean matches(ItemStack check) {
        if (stack.getItem() != check.getItem()) return false;
        if (matchDamage && (stack.getItemDamage() != check.getItemDamage())) return false;
        if (matchNBT) {
            if (!stack.hasTagCompound() && check.hasTagCompound()) return false;
            if (stack.hasTagCompound() && !stack.getTagCompound().equals(check.getTagCompound())) return false;
        }

        return true;
    }
}
