package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VillagerTradeTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = SimpleTrigger.createId("villager_trade");
    private final EntityPredicate villager;
    private final ItemPredicate tradedItem;
    public VillagerTradeTrigger(PlayerPredicate player, EntityPredicate villager, ItemPredicate tradedItem) {
        super(ID, player);
        this.villager = villager;
        this.tradedItem = tradedItem;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... addArgs) {
        return this.trigger(player, listener -> this.villager.matches((Entity) addArgs[0], player) && this.tradedItem.matches((ItemStack) addArgs[1]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("villager", this.villager.toJson());
        data.add("traded_item", this.tradedItem.toJson());
    }

    public static VillagerTradeTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new VillagerTradeTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EntityPredicate.fromJson(jsonObject.get("villager")), ItemPredicate.fromJson(jsonObject.get("traded_item")));
    }

    @SubscribeEvent
    public static void onVillagerTrade(final TradeWithVillagerEvent event) {
        fireTrigger(VillagerTradeTrigger.class, event.getEntity(), event.getAbstractVillager(), event.getMerchantOffer().getResult());
    }
}
