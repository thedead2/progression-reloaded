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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlayerHurtCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("player_hurt");

    private final DamageSourcePredicate damageSource;


    public PlayerHurtCriterionTrigger(EntityPredicate entity, DamageSourcePredicate damageSource) {
        this(entity, damageSource, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerHurtCriterionTrigger(EntityPredicate entity, DamageSourcePredicate damageSource, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, entity, amount, duration, "entity");
        this.damageSource = damageSource;
    }


    protected static PlayerHurtCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerHurtCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("entity")), DamageSourcePredicate.fromJson(jsonObject.get("damage_source")), amount, duration);
    }


    @SubscribeEvent
    public static void onEntityHurt(final LivingHurtEvent event) {
        fireTrigger(PlayerHurtCriterionTrigger.class, event.getEntity(), event.getSource().getEntity(), event.getSource());
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.damageSource.matches((DamageSource) data[0], player));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("damage_source", this.damageSource.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Get hurt from ").append(this.predicate.getDefaultDescription()).append(" by ").append(this.damageSource.getDefaultDescription());
    }
}
