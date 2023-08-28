package de.thedead2.progression_reloaded.data.predicates;


import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
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


    public DistancePredicate(MinMax.Doubles pX, MinMax.Doubles pY, MinMax.Doubles pZ, MinMax.Doubles pHorizontal, MinMax.Doubles pAbsolute) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.horizontal = pHorizontal;
        this.absolute = pAbsolute;
    }


    public static DistancePredicate fromJson(@Nullable JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "distance");
            MinMax.Doubles MinMaxNumber$doubles = MinMax.Doubles.fromJson(jsonobject.get("x"));
            MinMax.Doubles MinMaxNumber$doubles1 = MinMax.Doubles.fromJson(jsonobject.get("y"));
            MinMax.Doubles MinMaxNumber$doubles2 = MinMax.Doubles.fromJson(jsonobject.get("z"));
            MinMax.Doubles MinMaxNumber$doubles3 = MinMax.Doubles.fromJson(jsonobject.get("horizontal"));
            MinMax.Doubles MinMaxNumber$doubles4 = MinMax.Doubles.fromJson(jsonobject.get("absolute"));
            return new DistancePredicate(
                    MinMaxNumber$doubles,
                    MinMaxNumber$doubles1,
                    MinMaxNumber$doubles2,
                    MinMaxNumber$doubles3,
                    MinMaxNumber$doubles4
            );
        }
        else {
            return ANY;
        }
    }


    public static DistancePredicate from(DistanceInfo distanceInfo) {
        if(distanceInfo == null) {
            return ANY;
        }
        float xDistance = distanceInfo.getxDistance();
        float yDistance = distanceInfo.getyDistance();
        float zDistance = distanceInfo.getzDistance();

        return new DistancePredicate(
                MinMax.Doubles.exactly(xDistance),
                MinMax.Doubles.exactly(yDistance),
                MinMax.Doubles.exactly(zDistance),
                MinMax.Doubles.ANY,
                MinMax.Doubles.ANY
        );
    }


    @Override
    public boolean matches(DistanceInfo distanceInfo, Object... addArgs) {
        float xDistance = distanceInfo.getxDistance();
        float yDistance = distanceInfo.getyDistance();
        float zDistance = distanceInfo.getzDistance();
        if(this.x.matches(Mth.abs(xDistance)) && this.y.matches(Mth.abs(yDistance)) && this.z.matches(Mth.abs(zDistance))) {
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
            jsonobject.add("x", this.x.serializeToJson());
            jsonobject.add("y", this.y.serializeToJson());
            jsonobject.add("z", this.z.serializeToJson());
            jsonobject.add("horizontal", this.horizontal.serializeToJson());
            jsonobject.add("absolute", this.absolute.serializeToJson());
            return jsonobject;
        }
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


        public float getxDistance() {
            return xDistance;
        }


        public float getyDistance() {
            return yDistance;
        }


        public float getzDistance() {
            return zDistance;
        }
    }
}
