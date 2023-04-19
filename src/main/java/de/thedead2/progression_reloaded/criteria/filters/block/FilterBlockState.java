package de.thedead2.progression_reloaded.criteria.filters.block;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.block.Block;

@ProgressionRule(name="blockState", color=0xFF663300)
public class FilterBlockState extends FilterBaseBlock {
    public int blockState = 1;

    @Override
    protected boolean matches(Block block, int meta) {
        return blockState == meta;
    }
}
