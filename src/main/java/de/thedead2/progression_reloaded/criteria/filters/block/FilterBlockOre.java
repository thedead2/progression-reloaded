package de.thedead2.progression_reloaded.criteria.filters.block;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.criteria.filters.item.FilterItemOre;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import static de.thedead2.progression_reloaded.criteria.filters.block.FilterBaseBlock.getBlock;

@ProgressionRule(name="blockOre", color=0xFF663300)
public class FilterBlockOre extends FilterItemOre {
    @Override
    public boolean matches(ItemStack check) {
        Block block = getBlock(check);
        return block == null ? false : super.matches(check);
    }

    @Override
    public IFilterType getType() {
        return ProgressionAPI.filters.getBlockFilter();
    }
}
