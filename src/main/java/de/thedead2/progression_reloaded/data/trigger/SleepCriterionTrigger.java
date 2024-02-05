package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.LocationPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


//FIXME: Fire only when player really slept and woke up
public class SleepCriterionTrigger extends SimpleCriterionTrigger<BlockPos> {

    public static final ResourceLocation ID = createId("sleep");


    public SleepCriterionTrigger(MinMax.Ints amount, MinMax.Doubles duration) {
        this(LocationPredicate.ANY, amount, duration);
    }


    public SleepCriterionTrigger(LocationPredicate location, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, location, amount, duration, "sleep_location");
    }


    public SleepCriterionTrigger(LocationPredicate location) {
        this(location, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    protected static SleepCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        LocationPredicate location = LocationPredicate.fromJson(jsonObject.get("sleep_location"));
        return new SleepCriterionTrigger(location, amount, duration);
    }


    @SubscribeEvent
    public static void onPlayerWakeUp(final PlayerSleepInBedEvent event) {
        fireTrigger(SleepCriterionTrigger.class, event.getEntity(), event.getPos(), event.getEntity().getLevel());
    }


    @Override
    public boolean trigger(PlayerData player, BlockPos pos, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(pos, data[0]));
    }

    @Override
    public Component getDefaultDescription() {
        return Component.literal("Sleep ").append(this.predicate.getDefaultDescription());
    }
}
