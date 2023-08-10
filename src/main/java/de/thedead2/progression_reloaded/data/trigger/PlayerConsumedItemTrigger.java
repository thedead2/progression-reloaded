package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.annotation.ExcludeFromEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@ExcludeFromEventBus
public class PlayerConsumedItemTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("consumed_item");
    private final ItemPredicate consumedItem;
    public PlayerConsumedItemTrigger(PlayerPredicate player, ItemPredicate consumedItem) {
        super(ID, player);
        this.consumedItem = consumedItem;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... data) {
        return this.trigger(player, listener -> this.consumedItem.matches((ItemStack) data[0]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("consumed_item", this.consumedItem.toJson());
    }

    public static PlayerConsumedItemTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerConsumedItemTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), ItemPredicate.fromJson(jsonObject.get("consumed_item")));
    }

    public static void onItemConsumed(ServerPlayer player, ItemStack itemStack){
        fireTrigger(PlayerConsumedItemTrigger.class, player, itemStack);
    }
}
