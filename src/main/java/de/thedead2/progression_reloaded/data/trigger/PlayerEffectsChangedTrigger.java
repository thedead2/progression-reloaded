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
public class PlayerEffectsChangedTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("effects_changed");
    private final EffectsPredicate effects;
    private final EntityPredicate sourceEntity;
    public PlayerEffectsChangedTrigger(PlayerPredicate player, EffectsPredicate effects, EntityPredicate sourceEntity) {
        super(ID, player);
        this.effects = effects;
        this.sourceEntity = sourceEntity;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... data) {
        return this.trigger(player, listener -> this.effects.matches(player.getServerPlayer().getActiveEffectsMap()) && this.sourceEntity.matches((Entity) data[0], player));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("effects", this.effects.toJson());
        data.add("sourceEntity", this.sourceEntity.toJson());
    }

    public static PlayerEffectsChangedTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerEffectsChangedTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EffectsPredicate.fromJson(jsonObject.get("effects")), EntityPredicate.fromJson(jsonObject.get("sourceEntity")));
    }

    public static void onEffectsChanged(ServerPlayer player, Entity source){
        fireTrigger(PlayerEffectsChangedTrigger.class, player, source);
    }
}
