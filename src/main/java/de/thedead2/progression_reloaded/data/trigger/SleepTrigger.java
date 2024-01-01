package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.LocationPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


//FIXME: Fire only when player really slept and woke up
public class SleepTrigger extends SimpleTrigger<BlockPos> {

    public static final ResourceLocation ID = createId("sleep");


    public SleepTrigger(PlayerPredicate player) {
        this(player, LocationPredicate.ANY);
    }


    public SleepTrigger(PlayerPredicate player, LocationPredicate location) {
        super(ID, player, location, "sleep_location");
    }


    public static SleepTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        LocationPredicate location = LocationPredicate.fromJson(jsonObject.get("sleep_location"));
        return new SleepTrigger(player, location);
    }


    @SubscribeEvent
    public static void onPlayerWakeUp(final PlayerSleepInBedEvent event) {
        fireTrigger(SleepTrigger.class, event.getEntity(), event.getPos(), event.getEntity().getLevel());
    }


    @Override
    public boolean trigger(PlayerData player, BlockPos pos, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(pos, data[0]));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
