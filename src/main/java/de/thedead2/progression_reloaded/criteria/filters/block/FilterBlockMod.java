package de.thedead2.progression_reloaded.criteria.filters.block;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.helpers.StackHelper;
import net.minecraft.block.Block;

@ProgressionRule(name="blockMod", color=0xFF663300)
public class FilterBlockMod extends FilterBaseBlock {
    public String modid = "minecraft";

    @Override
    protected boolean matches(Block block, int meta) {
        if (modid.equals("*")) return true;
        return StackHelper.getModFromBlock(block).equals(modid);
    }
}
