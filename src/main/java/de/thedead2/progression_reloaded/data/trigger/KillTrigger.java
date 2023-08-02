package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.DamagePredicate;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class KillTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("kill");
    private final EntityPredicate entity;
    private final DamagePredicate damage;

    public KillTrigger(PlayerPredicate player, EntityPredicate entity, DamagePredicate damage) {
        super(ID, player);
        this.entity = entity;
        this.damage = damage;
    }
    public KillTrigger(PlayerPredicate player, EntityPredicate entity) {
        this(player, entity, null);
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, trigger -> this.entity.matches(((LivingEntity) data[0]).getType()) && (this.damage == null || this.damage.matches((DamageSource) data[1])));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("killed_entity", this.entity.toJson());
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
