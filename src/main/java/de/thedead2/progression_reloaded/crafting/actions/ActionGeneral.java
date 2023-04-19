package de.thedead2.progression_reloaded.crafting.actions;

import de.thedead2.progression_reloaded.crafting.ActionType;
import de.thedead2.progression_reloaded.crafting.Crafter;
import de.thedead2.progression_reloaded.crafting.CraftingRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ActionGeneral extends ActionForgeEvent {
    public static final ActionGeneral INSTANCE = new ActionGeneral();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttack(AttackEntityEvent event) {
        checkAndCancelEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkAndCancelEvent(event);
    }

    public boolean checkAndCancelEvent(net.minecraftforge.event.entity.player.PlayerEvent event) {
        //TODO: Check if get active stack is correct for here
        if (event.getEntityPlayer().getActiveItemStack() == null) return true;
        EntityPlayer player = event.getEntityPlayer();
        Crafter crafter = CraftingRegistry.get(player.worldObj.isRemote).getCrafterFromPlayer(player); //TODO: Check if get active stack is correct for here
        if (!crafter.canUseItemWithAction(event.getEntityPlayer().worldObj, ActionType.GENERAL, player.getActiveItemStack())) {
            event.setCanceled(true);
            return false;
        } else return true;
    }
}
