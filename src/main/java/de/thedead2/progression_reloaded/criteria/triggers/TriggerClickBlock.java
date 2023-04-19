package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IMiniIcon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static de.thedead2.progression_reloaded.ItemProgression.ItemMeta.click;
import static de.thedead2.progression_reloaded.ItemProgression.getStackFromMeta;

@ProgressionRule(name = "clickBlock", color = 0xFF69008C, cancelable = true)
public class TriggerClickBlock extends TriggerBaseBlock implements IMiniIcon {
    private static final ItemStack mini = getStackFromMeta(click);

    @Override
    public ITrigger copy() {
        return copyCounter(copyFilter(new TriggerClickBlock()));
    }

    @Override
    public ItemStack getMiniIcon() {
        return mini;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(PlayerInteractEvent event) {
        if (event.getPos() != null) {
            IBlockState state = event.getWorld().getBlockState(event.getPos());
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);
    
            if (ProgressionAPI.registry.fireTrigger(event.getEntityPlayer(), getProvider().getUnlocalisedName(), block, meta) == Result.DENY) {
                event.setCanceled(true);
            }
        }
    }
}
