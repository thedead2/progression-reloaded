package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.LocationPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class SleepTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("sleep");
    private final LocationPredicate location;

    public SleepTrigger(PlayerPredicate player) {
        this(player, LocationPredicate.ANY);
    }
    public SleepTrigger(PlayerPredicate player, LocationPredicate location) {
        super(ID, player);
        this.location = location;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, trigger -> this.location.matches((BlockPos) data[0]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("location", this.location.toJson());
    }

    public static SleepTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        LocationPredicate location = LocationPredicate.fromJson(jsonObject.get("location"));
        return new SleepTrigger(player, location);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(final PlayerSleepInBedEvent event){
        fireTrigger(SleepTrigger.class, event.getEntity(), event.getPos());
    }
}
