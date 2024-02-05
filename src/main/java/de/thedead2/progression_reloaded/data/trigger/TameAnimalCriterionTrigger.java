package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class TameAnimalCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = SimpleCriterionTrigger.createId("tame_animal");


    public TameAnimalCriterionTrigger(EntityPredicate tamedAnimal) {
        this(tamedAnimal, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public TameAnimalCriterionTrigger(EntityPredicate tamedAnimal, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, tamedAnimal, amount, duration, "tamed_animal");
    }


    protected static TameAnimalCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new TameAnimalCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("tamed_animal")), amount, duration);
    }


    @SubscribeEvent
    public static void onAnimalTamed(final AnimalTameEvent event) {
        fireTrigger(TameAnimalCriterionTrigger.class, event.getTamer(), event.getAnimal());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player));
    }

    @Override
    public Component getDefaultDescription() {
        return Component.literal("Tame ").append(this.predicate.getDefaultDescription());
    }
}
