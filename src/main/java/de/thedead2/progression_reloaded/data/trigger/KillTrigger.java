package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.DamageSourcePredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


//TODO: Add MinMaxInt for count/amount
public class KillTrigger extends SimpleTrigger<Entity> {

    public static final ResourceLocation ID = createId("kill");

    private final DamageSourcePredicate damage;


    public KillTrigger(PlayerPredicate player, EntityPredicate killedEntity) {
        this(player, killedEntity, null);
    }


    public KillTrigger(PlayerPredicate player, EntityPredicate killedEntity, DamageSourcePredicate damage) {
        super(ID, player, killedEntity, "killed_entity");
        this.damage = damage;
    }


    public static KillTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        EntityPredicate killed_entity = EntityPredicate.fromJson(jsonObject.get("killed_entity"));
        DamageSourcePredicate dealt_damage = jsonObject.has("damage_source") ? DamageSourcePredicate.fromJson(jsonObject.get("damage_source")) : null;

        return new KillTrigger(player, killed_entity, dealt_damage);
    }


    @SubscribeEvent
    public static void onEntityDeath(final LivingDeathEvent event) {
        fireTrigger(KillTrigger.class, event.getSource().getEntity(), event.getEntity(), event.getSource());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && (this.damage == null || this.damage.matches((DamageSource) data[0])));
    }


    @Override
    public void toJson(JsonObject data) {
        if(this.damage != null) {
            data.add("damage_source", this.damage.toJson());
        }
    }
}
