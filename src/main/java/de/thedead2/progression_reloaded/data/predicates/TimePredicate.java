package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;


public class TimePredicate implements ITriggerPredicate<Void> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("time");

    private final long ticks;

    private long ticksLeft;


    public TimePredicate(long ticks) {
        this(ticks, ticks);
    }


    private TimePredicate(long ticks, long ticksLeft) {
        this.ticks = ticks;
        this.ticksLeft = ticksLeft;
    }


    public static TimePredicate fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        long ticks = jsonObject.get("ticks").getAsLong();
        long ticksLeft = jsonObject.get("ticksLeft").getAsLong();
        return new TimePredicate(ticks, ticksLeft);
    }


    @Override
    public boolean matches(Void unused, Object... addArgs) {
        if(this.ticksLeft < 0) {
            return true;
        }
        else {
            this.ticksLeft--;
            return false;
        }
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ticks", this.ticks);
        jsonObject.addProperty("ticksLeft", this.ticksLeft);
        return jsonObject;
    }
}
