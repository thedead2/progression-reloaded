package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.annotation.ExcludeFromEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


@ExcludeFromEventBus
public class ItemEnchantedTrigger extends SimpleTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("item_enchanted");

    private final MinMax.Ints levels;


    public ItemEnchantedTrigger(PlayerPredicate player, ItemPredicate item, MinMax.Ints levels) {
        super(ID, player, item, "enchanted_item");
        this.levels = levels;
    }


    public static ItemEnchantedTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ItemEnchantedTrigger(
                PlayerPredicate.fromJson(jsonObject.get("player")),
                ItemPredicate.fromJson(jsonObject.get("enchanted_item")),
                MinMax.Ints.fromJson(jsonObject.get("levels"))
        );
    }


    public static void onItemEnchanted(ServerPlayer player, ItemStack item, int levels) {
        fireTrigger(ItemEnchantedTrigger.class, player, item, levels);
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack) && this.levels.matches((int) data[0]));
    }


    @Override
    public void toJson(JsonObject data) {
        data.add("levels", this.levels.serializeToJson());
    }
}
