package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class ItemPickupCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("item_pickup");


    public ItemPickupCriterionTrigger(ItemPredicate item) {
        this(item, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public ItemPickupCriterionTrigger(ItemPredicate item, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, item, amount, duration, "item");
    }


    protected static ItemPickupCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ItemPickupCriterionTrigger(ItemPredicate.fromJson(jsonObject.get("item")), amount, duration);
    }


    @SubscribeEvent
    public static void onItemPickup(final PlayerEvent.ItemPickupEvent event) {
        fireTrigger(ItemPickupCriterionTrigger.class, event.getEntity(), event.getStack());
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack item, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(item));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Pick up ").append(this.predicate.getDefaultDescription());
    }
}
