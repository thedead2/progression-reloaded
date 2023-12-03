package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;


public class TimePredicate implements ITriggerPredicate<Void> {

    private final long ticks;

    private long ticksLeft;


    public TimePredicate(long ticks) {
        this.ticks = ticks;
        this.ticksLeft = ticks;
    }


    public static TimePredicate fromJson(JsonElement jsonElement) {
        return new TimePredicate(jsonElement.getAsLong());
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
        return new JsonPrimitive(this.ticks);
    }
}
