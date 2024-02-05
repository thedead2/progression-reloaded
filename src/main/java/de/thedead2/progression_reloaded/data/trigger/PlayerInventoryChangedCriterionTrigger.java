package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.annotation.ExcludeFromEventBus;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


@ExcludeFromEventBus
public class PlayerInventoryChangedCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("inventory_changed");


    public PlayerInventoryChangedCriterionTrigger(ItemPredicate item) {
        this(item, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerInventoryChangedCriterionTrigger(ItemPredicate item, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, item, amount, duration, "new_item");
    }


    protected static PlayerInventoryChangedCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerInventoryChangedCriterionTrigger(ItemPredicate.fromJson(jsonObject.get("new_item")), amount, duration);
    }


    public static void onInventoryChanged(ServerPlayer player, ItemStack changedItem) {
        fireTrigger(PlayerInventoryChangedCriterionTrigger.class, player, changedItem);
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack toTest, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(toTest));
    }

    @Override
    public Component getDefaultDescription() {
        return Component.literal("Have ").append(this.predicate.getDefaultDescription()).append(" in your inventory");
    }
}
