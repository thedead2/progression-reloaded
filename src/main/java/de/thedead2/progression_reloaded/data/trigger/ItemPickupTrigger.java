package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemPickupTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("item_pickup");
    private final ItemPredicate item;
    public ItemPickupTrigger(PlayerPredicate player, ItemPredicate item) {
        super(ID, player);
        this.item = item;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... data) {
        return this.trigger(player, listener -> this.item.matches((ItemStack) data[0]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("item", this.item.toJson());
    }

    public static ItemPickupTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ItemPickupTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), ItemPredicate.fromJson(jsonObject.get("item")));
    }

    @SubscribeEvent
    public static void onItemPickup(final PlayerEvent.ItemPickupEvent event){
        fireTrigger(ItemPickupTrigger.class, event.getEntity(), event.getStack());
    }
}
