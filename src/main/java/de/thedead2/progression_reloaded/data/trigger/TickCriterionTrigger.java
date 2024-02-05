package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;


public class TickCriterionTrigger extends SimpleCriterionTrigger<Void> {

    public static final ResourceLocation ID = createId("tick");


    public TickCriterionTrigger() {
        this(MinMax.Doubles.ANY);
    }


    public TickCriterionTrigger(MinMax.Doubles duration) {
        super(ID, null, MinMax.Ints.ANY, duration, "");
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == LogicalSide.SERVER) {
            fireTrigger(TickCriterionTrigger.class, event.player, null);
        }
    }


    protected static TickCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        return new TickCriterionTrigger(duration);
    }


    @Override
    public boolean trigger(PlayerData player, Void toTest, Object... data) {
        return this.trigger(player, listener -> true);
    }

    @Override
    public Component getDefaultDescription() {
        return Component.literal("Wait ").append(this.duration.getDefaultDescription());
    }
}
