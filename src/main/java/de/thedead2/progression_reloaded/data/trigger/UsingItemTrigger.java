package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class UsingItemTrigger extends SimpleTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("using_item");


    public UsingItemTrigger(PlayerPredicate player, ItemPredicate item) {
        super(ID, player, item, "used_item");
    }


    public static UsingItemTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        ItemPredicate item = ItemPredicate.fromJson(jsonObject.get("used_item"));
        return new UsingItemTrigger(player, item);
    }


    @SubscribeEvent
    public static void onItemUse(final LivingEntityUseItemEvent.Start event) {
        fireTrigger(UsingItemTrigger.class, event.getEntity(), event.getItem());
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
