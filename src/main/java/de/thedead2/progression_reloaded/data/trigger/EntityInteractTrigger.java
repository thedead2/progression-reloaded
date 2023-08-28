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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class EntityInteractTrigger extends SimpleTrigger<Entity> {

    public static final ResourceLocation ID = createId("entity_interact");

    private final ItemPredicate itemInHand;


    public EntityInteractTrigger(PlayerPredicate player, EntityPredicate spawnedEntity, ItemPredicate itemInHand) {
        super(ID, player, spawnedEntity, "entity");
        this.itemInHand = itemInHand;
    }


    public static EntityInteractTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new EntityInteractTrigger(
                PlayerPredicate.fromJson(jsonObject.get("player")),
                EntityPredicate.fromJson(jsonObject.get("entity")),
                ItemPredicate.fromJson(jsonObject.get("item_in_hand"))
        );
    }


    @SubscribeEvent
    public static void onEntityInteraction(final PlayerInteractEvent.EntityInteract event) {
        fireTrigger(EntityInteractTrigger.class, event.getEntity(), event.getTarget(), event.getItemStack());
    }


    @Override
    public boolean trigger(SinglePlayer player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.itemInHand.matches((ItemStack) data[0]));
    }


    @Override
    public void toJson(JsonObject data) {
        data.add("item_in_hand", this.itemInHand.toJson());
    }
}
