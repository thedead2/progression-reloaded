package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.predicates.TimePredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;


public class TickTrigger extends SimpleTrigger<Void> {

    public static final ResourceLocation ID = createId("tick");


    public TickTrigger(PlayerPredicate player, TimePredicate predicate) {
        super(ID, player, predicate, "time");
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) {
            fireTrigger(TickTrigger.class, event.player, null);
        }
    }


    public static TickTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        TimePredicate time = TimePredicate.fromJson(jsonObject.get("time"));
        return new TickTrigger(player, time);
    }


    @Override
    public boolean trigger(PlayerData player, Void toTest, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(toTest, data));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
