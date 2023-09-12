package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlayerBredAnimalsTrigger extends SimpleTrigger<Entity> {

    public static final ResourceLocation ID = createId("bred_animals");

    private final EntityPredicate parentB;

    private final EntityPredicate child;


    public PlayerBredAnimalsTrigger(PlayerPredicate player, EntityPredicate parentA, EntityPredicate parentB, EntityPredicate child) {
        super(ID, player, parentA, "parentA");
        this.parentB = parentB;
        this.child = child;
    }


    public static PlayerBredAnimalsTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerBredAnimalsTrigger(
                PlayerPredicate.fromJson(jsonObject.get("player")),
                EntityPredicate.fromJson(jsonObject.get("parentA")),
                EntityPredicate.fromJson(jsonObject.get("parentB")),
                EntityPredicate.fromJson(jsonObject.get("child"))
        );
    }


    @SubscribeEvent
    public static void onAnimalsBred(final BabyEntitySpawnEvent event) {
        fireTrigger(PlayerBredAnimalsTrigger.class, event.getCausedByPlayer(), event.getParentA(), event.getParentB(), event.getChild());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.parentB.matches((Entity) data[0], player) && this.child.matches((Entity) data[1], player));
    }


    @Override
    public void toJson(JsonObject data) {
        data.add("parentB", this.parentB.toJson());
        data.add("child", this.child.toJson());
    }
}
