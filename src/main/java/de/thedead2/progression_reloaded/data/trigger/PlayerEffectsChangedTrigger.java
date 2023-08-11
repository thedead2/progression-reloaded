package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EffectsPredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.annotation.ExcludeFromEventBus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@ExcludeFromEventBus
public class PlayerEffectsChangedTrigger extends SimpleTrigger<Entity>{
    public static final ResourceLocation ID = createId("effects_changed");
    private final EffectsPredicate effects;
    public PlayerEffectsChangedTrigger(PlayerPredicate player, EffectsPredicate effects, EntityPredicate sourceEntity) {
        super(ID, player, sourceEntity, "source_entity");
        this.effects = effects;
    }

    @Override
    public boolean trigger(SinglePlayer player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.effects.matches(player.getServerPlayer().getActiveEffectsMap()) && this.predicate.matches(entity, player));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("effects", this.effects.toJson());
    }

    public static PlayerEffectsChangedTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerEffectsChangedTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EffectsPredicate.fromJson(jsonObject.get("effects")), EntityPredicate.fromJson(jsonObject.get("source_entity")));
    }

    public static void onEffectsChanged(ServerPlayer player, Entity source){
        fireTrigger(PlayerEffectsChangedTrigger.class, player, source);
    }
}
