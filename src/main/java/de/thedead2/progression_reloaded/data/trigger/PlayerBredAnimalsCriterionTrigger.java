package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlayerBredAnimalsCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("bred_animals");

    private final EntityPredicate parentB;

    private final EntityPredicate child;


    public PlayerBredAnimalsCriterionTrigger(EntityPredicate parentA, EntityPredicate parentB, EntityPredicate child) {
        this(parentA, parentB, child, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerBredAnimalsCriterionTrigger(EntityPredicate parentA, EntityPredicate parentB, EntityPredicate child, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, parentA, amount, duration, "parentA");
        this.parentB = parentB;
        this.child = child;
    }


    protected static PlayerBredAnimalsCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerBredAnimalsCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("parentA")), EntityPredicate.fromJson(jsonObject.get("parentB")), EntityPredicate.fromJson(jsonObject.get("child")), amount, duration);
    }


    @SubscribeEvent
    public static void onAnimalsBred(final BabyEntitySpawnEvent event) {
        fireTrigger(PlayerBredAnimalsCriterionTrigger.class, event.getCausedByPlayer(), event.getParentA(), event.getParentB(), event.getChild());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.parentB.matches((Entity) data[0], player) && this.child.matches((Entity) data[1], player));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("parentB", this.parentB.toJson());
        jsonObject.add("child", this.child.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Breed ").append(this.child.getDefaultDescription()).append(" from ").append(this.predicate.getDefaultDescription()).append(" and ").append(this.parentB.getDefaultDescription());
    }
}
