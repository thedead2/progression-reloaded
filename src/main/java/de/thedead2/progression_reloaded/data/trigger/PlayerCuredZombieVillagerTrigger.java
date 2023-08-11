package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerCuredZombieVillagerTrigger extends SimpleTrigger<Entity>{
    public static final ResourceLocation ID = createId("cured_villager");
    private final EntityPredicate newVillager;
    protected PlayerCuredZombieVillagerTrigger(PlayerPredicate player, EntityPredicate oldZombie, EntityPredicate newVillager) {
        super(ID, player, oldZombie, "zombie");
        this.newVillager = newVillager;
    }

    @Override
    public boolean trigger(SinglePlayer player, Entity entity, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(entity, player) && this.newVillager.matches((Entity) data[0], player));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("villager", this.newVillager.toJson());
    }

    public static PlayerCuredZombieVillagerTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new PlayerCuredZombieVillagerTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), EntityPredicate.fromJson(jsonObject.get("zombie")), EntityPredicate.fromJson(jsonObject.get("villager")));
    }

    @SubscribeEvent
    public static void onEntityConversion(final LivingConversionEvent.Post event){
        if(event.getEntity() instanceof ZombieVillager zombie){
            Player player = zombie.level.getPlayerByUUID(zombie.conversionStarter);
            fireTrigger(PlayerCuredZombieVillagerTrigger.class, player, zombie, event.getOutcome());
        }
    }
}
