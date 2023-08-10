package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.annotation.ExcludeFromEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@ExcludeFromEventBus
public class ItemEnchantedTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("item_enchanted");
    private final ItemPredicate item;
    private final MinMax.Ints levels;
    public ItemEnchantedTrigger(PlayerPredicate player, ItemPredicate item, MinMax.Ints levels) {
        super(ID, player);
        this.item = item;
        this.levels = levels;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... data) {
        return this.trigger(player, listener -> this.item.matches((ItemStack) data[0]) && this.levels.matches((int) data[1]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("enchanted_item", this.item.toJson());
        data.add("levels", this.levels.serializeToJson());
    }

    public static ItemEnchantedTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ItemEnchantedTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), ItemPredicate.fromJson(jsonObject.get("enchanted_item")), MinMax.Ints.fromJson(jsonObject.get("levels")));
    }

    public static void onItemEnchanted(ServerPlayer player, ItemStack item, int levels){
        fireTrigger(ItemEnchantedTrigger.class, player, item, levels);
    }
}
