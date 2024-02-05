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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class EntityInteractCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("entity_interact");

    private final ItemPredicate itemInHand;


    public EntityInteractCriterionTrigger(EntityPredicate spawnedEntity, ItemPredicate itemInHand) {
        this(spawnedEntity, itemInHand, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public EntityInteractCriterionTrigger(EntityPredicate spawnedEntity, ItemPredicate itemInHand, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, spawnedEntity, amount, duration, "entity");
        this.itemInHand = itemInHand;
    }


    protected static EntityInteractCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new EntityInteractCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("entity")), ItemPredicate.fromJson(jsonObject.get("item_in_hand")), amount, duration);
    }


    @SubscribeEvent
    public static void onEntityInteraction(final PlayerInteractEvent.EntityInteract event) {
        fireTrigger(EntityInteractCriterionTrigger.class, event.getEntity(), event.getTarget(), event.getItemStack());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.itemInHand.matches((ItemStack) data[0]));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("item_in_hand", this.itemInHand.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Interact with ").append(this.predicate.getDefaultDescription());
    }
}
