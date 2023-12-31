package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class TameAnimalTrigger extends SimpleTrigger<Entity> {

    public static final ResourceLocation ID = SimpleTrigger.createId("tame_animal");


    public TameAnimalTrigger(PlayerPredicate player, EntityPredicate tamedAnimal) {
        super(ID, player, tamedAnimal, "tamed_animal");
    }


    public static TameAnimalTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new TameAnimalTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EntityPredicate.fromJson(jsonObject.get("tamed_animal")));
    }


    @SubscribeEvent
    public static void onAnimalTamed(final AnimalTameEvent event) {
        fireTrigger(TameAnimalTrigger.class, event.getTamer(), event.getAnimal());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
