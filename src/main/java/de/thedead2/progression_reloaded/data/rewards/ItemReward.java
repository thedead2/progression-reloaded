package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerPlayer;
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
    public void rewardPlayer(ServerPlayer player) {
        this.item.setCount(this.amount);
        player.getInventory().add(this.item);
    }
}
