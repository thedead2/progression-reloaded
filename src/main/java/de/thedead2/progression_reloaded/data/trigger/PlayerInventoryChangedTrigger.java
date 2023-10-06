package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.annotation.ExcludeFromEventBus;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


@ExcludeFromEventBus
public class PlayerInventoryChangedTrigger extends SimpleTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("inventory_changed");


    public PlayerInventoryChangedTrigger(PlayerPredicate player, ItemPredicate item) {
        super(ID, player, item, "new_item");
    }


    public static PlayerInventoryChangedTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerInventoryChangedTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), ItemPredicate.fromJson(jsonObject.get("new_item")));
    }


    public static void onInventoryChanged(ServerPlayer player, ItemStack changedItem) {
        fireTrigger(PlayerInventoryChangedTrigger.class, player, changedItem);
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack toTest, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(toTest));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
