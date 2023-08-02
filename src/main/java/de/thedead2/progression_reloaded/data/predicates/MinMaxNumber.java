package de.thedead2.progression_reloaded.data.predicates;

import com.google.common.base.Objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MinMaxNumber<T extends Number>{
    public static final MinMaxNumber<? extends Number> ANY = new MinMaxNumber<>();
    private final T min;
    private final T max;

    public MinMaxNumber(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public MinMaxNumber(T min) {
        this(min, null);
    }

    public MinMaxNumber() {
        this(null, null);
    }

    public @Nullable T getMin() {
        return min;
    }

    public @Nullable T getMax() {
        return max;
    }

    public boolean isInRange(T in){
        if(this == ANY) return true;
        else {
            if(this.max != null){
                return this.min.floatValue() <= in.floatValue() && in.floatValue() >= this.max.floatValue();
            }
            else return this.min.floatValue() == in.floatValue();
        }
    }

    public JsonElement toJson(){
        JsonObject jsonObject = new JsonObject();
        if(this.min != null && this.max != null) {
            jsonObject.add("min", new JsonPrimitive(this.min));
            jsonObject.add("max", new JsonPrimitive(this.max));
            return jsonObject;
        }
        else if(this.min != null) return new JsonPrimitive(this.min);
        else return JsonNull.INSTANCE;
    }

    public static MinMaxNumber<?> fromJson(JsonElement jsonElement){
        if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.has("max")){
                return new MinMaxNumber<>(jsonObject.get("min").getAsNumber(), jsonObject.get("max").getAsNumber());
            }
        }
        else if(jsonElement.isJsonPrimitive()){
            return new MinMaxNumber<>(jsonElement.getAsJsonPrimitive().getAsNumber());
        }
        else if(jsonElement.isJsonNull()) return ANY;
        throw new IllegalArgumentException("Can't read JsonElement of type: " + jsonElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinMaxNumber<?> that = (MinMaxNumber<?>) o;
        return Objects.equal(min, that.min) && Objects.equal(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }

    public boolean matches(MinMaxNumber<T> num) {
        return this.equals(num);
    }
}
