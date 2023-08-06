package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class TickTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("tick");
    private final int delay; //in ticks
    private int counter = 0;
    public TickTrigger(PlayerPredicate player, int delay){
        super(ID, player);
        this.delay = delay;
    }

    public TickTrigger(PlayerPredicate player){
        this(player, 0);
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        if(counter < delay){
            counter++;
            return;
        }
        this.trigger(player, trigger -> true);
    }

    @Override
    public void toJson(JsonObject data) {
        if(this.delay != 0) data.add("delay", new JsonPrimitive(this.delay));
    }

    public static TickTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        int delay = jsonObject.has("delay") ? jsonObject.get("delay").getAsInt() : 0;
        return new TickTrigger(player, delay);
    }
    @SubscribeEvent
    public static void onPlayerTick(final LivingEvent.LivingTickEvent event){
        //fireTrigger(TickTrigger.class, event.getEntity());
    }
}
