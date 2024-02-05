package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;


public class DistancePredicate implements ITriggerPredicate<DistancePredicate.DistanceInfo> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("distance");

    public static final DistancePredicate ANY = new DistancePredicate(MinMax.Doubles.ANY, MinMax.Doubles.ANY, MinMax.Doubles.ANY, MinMax.Doubles.ANY, MinMax.Doubles.ANY);

    private final MinMax.Doubles x;

    private final MinMax.Doubles y;

    private final MinMax.Doubles z;

    private final MinMax.Doubles horizontal;

    private final MinMax.Doubles absolute;


    public DistancePredicate(MinMax.Doubles x, MinMax.Doubles y, MinMax.Doubles z, MinMax.Doubles horizontal, MinMax.Doubles absolute) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.horizontal = horizontal;
        this.absolute = absolute;
    }


    public static DistancePredicate fromJson(@Nullable JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();
            MinMax.Doubles x = MinMax.Doubles.fromJson(jsonobject.get("x"));
            MinMax.Doubles y = MinMax.Doubles.fromJson(jsonobject.get("y"));
            MinMax.Doubles z = MinMax.Doubles.fromJson(jsonobject.get("z"));
            MinMax.Doubles horizontal = MinMax.Doubles.fromJson(jsonobject.get("horizontal"));
            MinMax.Doubles absolute = MinMax.Doubles.fromJson(jsonobject.get("absolute"));

            return new DistancePredicate(x, y, z, horizontal, absolute);
        }
        else {
            return ANY;
        }
    }


    @Override
    public boolean matches(DistanceInfo distanceInfo, Object... addArgs) {
        float xDistance = distanceInfo.getXDistance();
        float yDistance = distanceInfo.getYDistance();
        float zDistance = distanceInfo.getZDistance();
        if(this.x.matches((double) Mth.abs(xDistance)) && this.y.matches((double) Mth.abs(yDistance)) && this.z.matches((double) Mth.abs(zDistance))) {
            if(!this.horizontal.matchesSqr(xDistance * xDistance + zDistance * zDistance)) {
                return false;
            }
            else {
                return this.absolute.matchesSqr(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
            }
        }
        else {
            return false;
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("x", this.x.toJson());
            jsonobject.add("y", this.y.toJson());
            jsonobject.add("z", this.z.toJson());
            jsonobject.add("horizontal", this.horizontal.toJson());
            jsonobject.add("absolute", this.absolute.toJson());
            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        return Component.empty();
    }


    public static class DistanceInfo {

        private final float xDistance;

        private final float yDistance;

        private final float zDistance;


        public DistanceInfo(Number x1, Number y1, Number z1, Number x2, Number y2, Number z2) {
            this(x1.floatValue() - x2.floatValue(), y1.floatValue() - y2.floatValue(), z1.floatValue() - z2.floatValue());
        }


        public DistanceInfo(float xDistance, float yDistance, float zDistance) {
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            this.zDistance = zDistance;
        }


        public float getXDistance() {
            return xDistance;
        }


        public float getYDistance() {
            return yDistance;
        }


        public float getZDistance() {
            return zDistance;
        }
    }
}
