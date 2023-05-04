package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemReward implements IReward{

    private final ItemStack item;
    private final int amount;

    public ItemReward(ItemStack item, int amount){
        this.item = item;
        this.amount = amount;
    }

    @Override
    public void rewardPlayer(Player player) {
        for(int i = 0; i <= this.amount; i++){
            player.getInventory().add(this.item);
        }
    }
}
