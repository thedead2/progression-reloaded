package de.thedead2.progression_reloaded.data.criteria;

import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.SinglePlayer;
import net.minecraft.world.item.ItemStack;

public class UsedItemTrigger extends SimpleTrigger implements ICriterion {
    private final ItemStack item;
    public UsedItemTrigger(ItemStack item){
        this.item = item;
    }

    @Override
    public SimpleTrigger getTrigger() {
        return this;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, (singlePlayer1) -> this.item.equals(data[0]));
    }
}
