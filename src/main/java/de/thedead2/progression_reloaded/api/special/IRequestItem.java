package de.thedead2.progression_reloaded.api.special;

import de.thedead2.progression_reloaded.api.criteria.IReward;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** This should only ever be implemented on rewards **/
public interface IRequestItem extends IReward {
    /** This should return an item that matches the filters **/
    public ItemStack getRequestedStack(EntityPlayer player);

    /** This should reward the player **/
    public void reward(EntityPlayer player, ItemStack stack);
}
