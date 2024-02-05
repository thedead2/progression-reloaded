package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.DamageSourcePredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class KillCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("kill");

    private final DamageSourcePredicate damage;


    public KillCriterionTrigger(EntityPredicate killedEntity, MinMax.Ints amount, MinMax.Doubles duration) {
        this(killedEntity, DamageSourcePredicate.ANY, amount, duration);
    }


    public KillCriterionTrigger(EntityPredicate killedEntity, DamageSourcePredicate damage, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, killedEntity, amount, duration, "killed_entity");
        this.damage = damage;
    }


    public KillCriterionTrigger(EntityPredicate killedEntity) {
        this(killedEntity, DamageSourcePredicate.ANY);
    }


    public KillCriterionTrigger(EntityPredicate killedEntity, DamageSourcePredicate damage) {
        this(killedEntity, damage, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    protected static KillCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        EntityPredicate killed_entity = EntityPredicate.fromJson(jsonObject.get("killed_entity"));
        DamageSourcePredicate dealt_damage = DamageSourcePredicate.fromJson(jsonObject.get("damage_source"));

        return new KillCriterionTrigger(killed_entity, dealt_damage, amount, duration);
    }


    @SubscribeEvent
    public static void onEntityDeath(final LivingDeathEvent event) {
        fireTrigger(KillCriterionTrigger.class, event.getSource().getEntity(), event.getEntity(), event.getSource());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && (this.damage == null || this.damage.matches((DamageSource) data[0])));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("damage_source", this.damage.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Kill ").append(this.amount.getDefaultDescription()).append(this.predicate.getDefaultDescription()).append(" by ").append(this.damage.getDefaultDescription());
    }
}
