package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class UsingItemCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("using_item");


    public UsingItemCriterionTrigger(ItemPredicate item) {
        this(item, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public UsingItemCriterionTrigger(ItemPredicate item, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, item, amount, duration, "used_item");
    }


    protected static UsingItemCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ItemPredicate item = ItemPredicate.fromJson(jsonObject.get("used_item"));
        return new UsingItemCriterionTrigger(item, amount, duration);
    }


    @SubscribeEvent
    public static void onItemUse(final LivingEntityUseItemEvent.Start event) {
        fireTrigger(UsingItemCriterionTrigger.class, event.getEntity(), event.getItem());
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Use ").append(this.predicate.getDefaultDescription());
    }
}
