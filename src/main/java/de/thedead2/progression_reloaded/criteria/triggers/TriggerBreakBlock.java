package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IMiniIcon;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static de.thedead2.progression_reloaded.ItemProgression.ItemMeta.breaking;
import static de.thedead2.progression_reloaded.ItemProgression.getStackFromMeta;

@ProgressionRule(name = "breakBlock", color = 0xFFDDDDDD, cancelable = true)
public class TriggerBreakBlock extends TriggerBaseBlock implements IMiniIcon {
    private static final ItemStack mini = getStackFromMeta(breaking);

    @Override
    public ITrigger copy() {
        return copyCounter(copyFilter(new TriggerBreakBlock()));
    }

    @Override
    public ItemStack getMiniIcon() {
        return mini;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(BreakEvent event) {
        Block block = event.getState().getBlock();
        int meta = block.getMetaFromState(event.getState());
        if (ProgressionAPI.registry.fireTrigger(event.getPlayer(), getProvider().getUnlocalisedName(), block, meta) == Result.DENY) {
            event.setCanceled(true);
        }
    }
}
