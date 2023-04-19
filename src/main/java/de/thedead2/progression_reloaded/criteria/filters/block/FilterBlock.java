package de.thedead2.progression_reloaded.criteria.filters.block;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IItemGetterCallback;
import de.thedead2.progression_reloaded.api.special.ISetterCallback;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

@ProgressionRule(name="blockOnly", color=0xFFCCCCCC)
public class FilterBlock extends FilterBaseBlock implements IItemGetterCallback, ISetterCallback, ISpecialFieldProvider {
    public Block filterBlock = Blocks.SANDSTONE;

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItem(this, "filterBlock", 25, 25, 3F));
    }

    @Override
    protected boolean matches(Block block, int meta) {
        return filterBlock == block;
    }

    @Override
    public ItemStack getItem(String fieldName) {
        return new ItemStack(filterBlock);
    }

    @Override
    public boolean setField(String fieldName, Object stack) {
        try {
            filterBlock = getBlock(((ItemStack) stack));
        } catch (Exception e) {}

        return true;
    }
}
