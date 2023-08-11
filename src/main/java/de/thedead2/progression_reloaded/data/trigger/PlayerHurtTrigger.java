package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.DamageSourcePredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerHurtTrigger extends SimpleTrigger<Entity>{
    public static final ResourceLocation ID = createId("player_hurt");
    private final DamageSourcePredicate damageSource;

    public PlayerHurtTrigger(PlayerPredicate player, EntityPredicate entity, DamageSourcePredicate damageSource) {
        super(ID, player, entity, "entity");
        this.damageSource = damageSource;
    }

    @Override
    public boolean trigger(SinglePlayer player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.damageSource.matches((DamageSource) data[0], player));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("damage_source", this.damageSource.toJson());
    }

    public static PlayerHurtTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerHurtTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EntityPredicate.fromJson(jsonObject.get("entity")), DamageSourcePredicate.fromJson(jsonObject.get("damage_source")));
    }

    @SubscribeEvent
    public static void onEntityHurt(final LivingHurtEvent event){
        fireTrigger(PlayerHurtTrigger.class, event.getEntity(), event.getSource().getEntity(), event.getSource());
    }
}
