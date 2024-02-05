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
public class PlayerConsumedItemCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("consumed_item");


    public PlayerConsumedItemCriterionTrigger(ItemPredicate consumedItem) {
        this(consumedItem, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerConsumedItemCriterionTrigger(ItemPredicate consumedItem, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, consumedItem, amount, duration, "consumed_item");
    }


    protected static PlayerConsumedItemCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerConsumedItemCriterionTrigger(ItemPredicate.fromJson(jsonObject.get("consumed_item")), amount, duration);
    }


    public static void onItemConsumed(ServerPlayer player, ItemStack itemStack) {
        fireTrigger(PlayerConsumedItemCriterionTrigger.class, player, itemStack);
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Consume ").append(this.predicate.getDefaultDescription());
    }
}
