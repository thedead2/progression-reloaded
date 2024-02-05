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


public class CraftingCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("crafting");


    public CraftingCriterionTrigger(ItemPredicate craftingResult) {
        this(craftingResult, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public CraftingCriterionTrigger(ItemPredicate craftingResult, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, craftingResult, amount, duration, "crafted_item");
    }


    protected static CraftingCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ItemPredicate crafted_item = ItemPredicate.fromJson(jsonObject.get("crafted_item"));

        return new CraftingCriterionTrigger(crafted_item, amount, duration);
    }


    @SubscribeEvent
    public static void onItemCrafted(final PlayerEvent.ItemCraftedEvent event) {
        fireTrigger(CraftingCriterionTrigger.class, event.getEntity(), event.getCrafting());
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack craftedItem, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(craftedItem));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Craft ").append(this.amount.getDefaultDescription()).append(this.predicate.getDefaultDescription());
    }
}
