package de.thedead2.progression_reloaded.crafting.actions;

import de.thedead2.progression_reloaded.crafting.ActionType;
import de.thedead2.progression_reloaded.handlers.ProgressionEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;

public class ActionHarvestDrop extends ActionForgeEvent {
    public static final ActionHarvestDrop INSTANCE = new ActionHarvestDrop();

    @SubscribeEvent
    public void onHarvestDrop(HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player != null) {
            Iterator<ItemStack> it = event.getDrops().iterator();
            while (it.hasNext()) {
                ItemStack stack = it.next();
                if (ProgressionEvents.isEventCancelled(player, ActionType.HARVESTDROPWITH, player.getHeldItemMainhand(), ActionType.HARVESTDROP, stack)) {
                    it.remove();
                }
            }
        }
    }
}
