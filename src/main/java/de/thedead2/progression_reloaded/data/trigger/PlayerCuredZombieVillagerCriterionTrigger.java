package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlayerCuredZombieVillagerCriterionTrigger extends SimpleCriterionTrigger<Entity> {

    public static final ResourceLocation ID = createId("cured_villager");

    private final EntityPredicate newVillager;


    protected PlayerCuredZombieVillagerCriterionTrigger(EntityPredicate oldZombie, EntityPredicate newVillager) {
        this(oldZombie, newVillager, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    protected PlayerCuredZombieVillagerCriterionTrigger(EntityPredicate oldZombie, EntityPredicate newVillager, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, oldZombie, amount, duration, "zombie");
        this.newVillager = newVillager;
    }


    protected static PlayerCuredZombieVillagerCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerCuredZombieVillagerCriterionTrigger(EntityPredicate.fromJson(jsonObject.get("zombie")), EntityPredicate.fromJson(jsonObject.get("villager")), amount, duration);
    }


    @SubscribeEvent
    public static void onEntityConversion(final LivingConversionEvent.Post event) {
        if(event.getEntity() instanceof ZombieVillager zombie) {
            Player player = zombie.level.getPlayerByUUID(zombie.conversionStarter);
            fireTrigger(PlayerCuredZombieVillagerCriterionTrigger.class, player, zombie, event.getOutcome());
        }
    }


    @Override
    public boolean trigger(PlayerData player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.newVillager.matches((Entity) data[0], player));
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        jsonObject.add("villager", this.newVillager.toJson());
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Cure a zombie villager");
    }
}
