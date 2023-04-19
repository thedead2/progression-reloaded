package de.thedead2.progression_reloaded.criteria.filters.block;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

@ProgressionRule(name="blockStack", color=0xFF663300)
public class FilterBlockStack extends FilterBaseBlock implements ISpecialFieldProvider, IInit {
    public ItemStack stack = new ItemStack(Blocks.ANVIL);
    public boolean matchState = true;
    private Block filterBlock = Blocks.ANVIL;
    private int filterMeta = 0;

    @Override
    public void init(boolean isClient) {
        try {
            filterBlock = getBlock(stack);
            filterMeta = filterBlock.getMetaFromState(filterBlock.getStateFromMeta((stack).getItemDamage()));
        } catch (Exception e) {}
    }

    @Override
    public ItemStack getRandom(EntityPlayer player) {
        return stack.copy();
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItem(this, "stack", 30, 35, 2.4F));
    }

    @Override
    protected boolean matches(Block block, int meta) {
        if (block != filterBlock) return false;
        if (matchState && (meta != filterMeta)) return false;
        return true;
    }
}
