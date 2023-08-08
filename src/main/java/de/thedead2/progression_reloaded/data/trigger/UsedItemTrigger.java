package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class UsedItemTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("used_item");
    private final ItemPredicate item;

    public UsedItemTrigger(PlayerPredicate player, ItemPredicate item){
        super(ID, player);
        this.item = item;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, listener -> this.item.matches((ItemStack) data[0]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("used_item", this.item.toJson());
    }

    public static UsedItemTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        ItemPredicate item = ItemPredicate.fromJson(jsonObject.get("used_item"));
        return new UsedItemTrigger(player, item);
    }

    @SubscribeEvent
    public static void onItemUse(final LivingEntityUseItemEvent.Start event){
        fireTrigger(UsedItemTrigger.class, event.getEntity(), event.getItem());
    }
}
