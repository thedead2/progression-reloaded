package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class VillagerTradeCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = SimpleCriterionTrigger.createId("villager_trade");

    private final ItemPredicate tradedItem;


    public VillagerTradeCriterionTrigger(EntityPredicate villager, ItemPredicate tradedItem) {
        this(villager, tradedItem, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public VillagerTradeCriterionTrigger(EntityPredicate villager, ItemPredicate tradedItem, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, villager, amount, duration, "villager");
        this.tradedItem = tradedItem;
    }


    protected static VillagerTradeCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new VillagerTradeCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("villager")), ItemPredicate.fromJson(jsonObject.get("traded_item")), amount, duration);
    }


    @SubscribeEvent
    public static void onVillagerTrade(final TradeWithVillagerEvent event) {
        fireTrigger(VillagerTradeCriterionTrigger.class, event.getEntity(), event.getAbstractVillager(), event.getMerchantOffer().getResult());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... addArgs) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.tradedItem.matches((ItemStack) addArgs[0]));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("traded_item", this.tradedItem.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Trade with a ").append(this.predicate.getDefaultDescription()).append(this.tradedItem.getDefaultDescription());
    }
}
