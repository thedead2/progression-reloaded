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
public class PlayerConsumedItemTrigger extends SimpleTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("consumed_item");


    public PlayerConsumedItemTrigger(PlayerPredicate player, ItemPredicate consumedItem) {
        super(ID, player, consumedItem, "consumed_item");
    }


    public static PlayerConsumedItemTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerConsumedItemTrigger(
                PlayerPredicate.fromJson(jsonObject.get("player")),
                ItemPredicate.fromJson(jsonObject.get("consumed_item"))
        );
    }


    public static void onItemConsumed(ServerPlayer player, ItemStack itemStack) {
        fireTrigger(PlayerConsumedItemTrigger.class, player, itemStack);
    }


    @Override
    public boolean trigger(SinglePlayer player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
