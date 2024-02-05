package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.annotation.ExcludeFromEventBus;
import de.thedead2.progression_reloaded.data.predicates.EnchantmentPredicate;
import de.thedead2.progression_reloaded.data.predicates.ItemPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


@ExcludeFromEventBus
public class ItemEnchantedCriterionTrigger extends SimpleCriterionTrigger<ItemStack> {

    public static final ResourceLocation ID = createId("item_enchanted");

    private final EnchantmentPredicate enchantment;


    public ItemEnchantedCriterionTrigger(ItemPredicate item, EnchantmentPredicate enchantment) {
        this(item, enchantment, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public ItemEnchantedCriterionTrigger(ItemPredicate item, EnchantmentPredicate enchantment, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, item, amount, duration, "enchanted_item");
        this.enchantment = enchantment;
    }


    protected static ItemEnchantedCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new ItemEnchantedCriterionTrigger(ItemPredicate.fromJson(jsonObject.get("enchanted_item")), EnchantmentPredicate.fromJson(jsonObject.get("enchantment")), amount, duration);
    }


    public static void onItemEnchanted(ServerPlayer player, ItemStack item, int levels) {
        fireTrigger(ItemEnchantedCriterionTrigger.class, player, item, levels);
    }


    @Override
    public boolean trigger(PlayerData player, ItemStack itemStack, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(itemStack));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("enchantment", this.enchantment.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Enchant ").append(this.predicate.getDefaultDescription()).append(" with ").append(this.enchantment.getDefaultDescription());
    }
}
