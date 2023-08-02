package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class CraftingTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("crafting");
    private final ItemPredicate craftingResult;
    private final int amountCrafted;
    private int craftCounter = 0;

    public CraftingTrigger(PlayerPredicate player, ItemPredicate craftingResult, int amountCrafted) {
        super(ID, player);
        this.craftingResult = craftingResult;
        this.amountCrafted = amountCrafted;
    }
    public CraftingTrigger(PlayerPredicate player, ItemPredicate craftingResult) {
        this(player, craftingResult, 1);
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        ItemStack craftedItem = (ItemStack) data[0];
        if(craftCounter < amountCrafted && this.craftingResult.matches(craftedItem)){
            craftCounter++;
            return;
        }
        this.trigger(player, trigger -> this.craftingResult.matches(craftedItem));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("crafted_item", this.craftingResult.toJson());
        if(this.amountCrafted != 1) data.add("amount", new JsonPrimitive(this.amountCrafted));
    }

    public static CraftingTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        ItemPredicate crafted_item = ItemPredicate.fromJson(jsonObject.get("crafted_item"));
        int amount = jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1;
        return new CraftingTrigger(player, crafted_item, amount);
    }

    @SubscribeEvent
    public static void onItemCrafted(final PlayerEvent.ItemCraftedEvent event){
        fireTrigger(CraftingTrigger.class, event.getEntity(), event.getCrafting());
    }
}
