package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.DamagePredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KillTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("kill");
    private final EntityPredicate killedEntity;
    private final DamagePredicate damage;

    public KillTrigger(PlayerPredicate player, EntityPredicate killedEntity, DamagePredicate damage) {
        super(ID, player);
        this.killedEntity = killedEntity;
        this.damage = damage;
    }
    public KillTrigger(PlayerPredicate player, EntityPredicate killedEntity) {
        this(player, killedEntity, null);
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, trigger -> this.killedEntity.matches(((Entity) data[0]), player) && (this.damage == null || this.damage.matches((DamageSource) data[1])));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("killed_entity", this.killedEntity.toJson());
        if(this.damage != null) data.add("dealt_damage", this.damage.toJson());
    }

    public static KillTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        EntityPredicate killed_entity = EntityPredicate.fromJson(jsonObject.get("killed_entity"));
        DamagePredicate dealt_damage = jsonObject.has("dealt_damage") ? DamagePredicate.fromJson(jsonObject.get("dealt_damage")) : null;
        return new KillTrigger(player, killed_entity, dealt_damage);
    }

    @SubscribeEvent
    public static void onEntityDeath(final LivingDeathEvent event){
        fireTrigger(KillTrigger.class, event.getSource().getEntity(), event.getEntity(), event.getSource());
    }
}
