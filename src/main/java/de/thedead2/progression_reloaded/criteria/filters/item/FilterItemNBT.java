package de.thedead2.progression_reloaded.criteria.filters.item;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.helpers.StackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@ProgressionRule(name="nbtString", color=0xFF00B2B2)
public class FilterItemNBT extends FilterBaseItem implements IInit {
    public NBTTagCompound tagValue = new NBTTagCompound();
    public String tagText = "";

    @Override
    public void init(boolean isClient) {
        tagValue = StackHelper.getTag(new String[] { tagText }, 0);
    }

    @Override
    public boolean matches(ItemStack check) {
        return check.hasTagCompound() && check.getTagCompound().equals(tagValue);
    }
}
