package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.annotation.ExcludeFromEventBus;
import de.thedead2.progression_reloaded.data.predicates.EffectsPredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;


@ExcludeFromEventBus
public class PlayerEffectsChangedCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("effects_changed");

    private final EffectsPredicate effects;


    public PlayerEffectsChangedCriterionTrigger(EffectsPredicate effects, EntityPredicate sourceEntity) {
        this(effects, sourceEntity, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerEffectsChangedCriterionTrigger(EffectsPredicate effects, EntityPredicate sourceEntity, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, sourceEntity, amount, duration, "source_entity");
        this.effects = effects;
    }


    protected static PlayerEffectsChangedCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerEffectsChangedCriterionTrigger(EffectsPredicate.fromJson(jsonObject.get("effects")), EntityPredicate.fromJson(jsonObject.get("source_entity")), amount, duration);
    }


    public static void onEffectsChanged(ServerPlayer player, Entity source) {
        fireTrigger(PlayerEffectsChangedCriterionTrigger.class, player, source);
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.effects.matches(player.getServerPlayer().getActiveEffectsMap()) && this.predicate.matches(entity, player));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("effects", this.effects.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Get ").append(this.effects.getDefaultDescription()).append(" from ").append(this.predicate.getDefaultDescription());
    }
}
