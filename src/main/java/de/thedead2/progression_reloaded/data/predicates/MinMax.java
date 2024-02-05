package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.api.INbtSerializable;
import de.thedead2.progression_reloaded.api.IStatusChecker;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.util.TickTimer;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public abstract class MinMax<T extends Number> {

    @Nullable
    protected final T min;

    @Nullable
    protected final T max;

    @Nullable
    protected final T minSq;

    @Nullable
    protected final T maxSq;


    protected MinMax(@Nullable T min, @Nullable T max) {
        this.min = min;
        this.max = max;
        this.minSq = squareOpt(min);
        this.maxSq = squareOpt(max);
    }


    @Nullable
    protected abstract T squareOpt(@Nullable T value);


    protected static <T extends Number, R extends MinMax<T>> R fromJson(@Nullable JsonElement jsonElement, R defaultValue, Function<JsonElement, T> valueConverter, MinMaxFactory<T, R> minMaxFactory) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            if(GsonHelper.isNumberValue(jsonElement)) {
                T value = valueConverter.apply(jsonElement);
                return minMaxFactory.create(value, value);
            }
            else {
                JsonObject jsonobject = jsonElement.getAsJsonObject();
                T min = SerializationHelper.getNullable(jsonobject, "min", valueConverter);
                T max = SerializationHelper.getNullable(jsonobject, "max", valueConverter);
                return minMaxFactory.create(min, max);
            }
        }
        else {
            return defaultValue;
        }
    }


    public Component getDefaultDescription() {
        if(this.isAny()) {
            return Component.empty();
        }

        if(this.min != null) {
            if(this.max == null) {
                return Component.literal("at least").append(this.min.toString());
            }
            else if(this.min.equals(this.max)) {
                return Component.literal(this.min.toString());
            }
            else {
                return Component.literal("between").append(this.min.toString()).append("and").append(this.max.toString());
            }
        }
        else {
            return Component.literal("at most").append(this.max.toString());
        }
    }


    @Nonnull
    Component getShortDescription() {
        if(this.isAny()) {
            return Component.empty();
        }

        if(this.min != null) {
            if(this.max == null) {
                return Component.literal(">").append(this.min.toString());
            }
            else if(this.min.equals(this.max)) {
                return Component.literal("/").append(this.min.toString());
            }
            else {
                return Component.literal(">").append(this.min.toString()).append("<").append(this.max.toString());
            }
        }
        else {
            return Component.literal("<").append(this.max.toString());
        }
    }


    public abstract boolean matches(T t);


    @Nullable
    public T getMin() {
        return this.min;
    }


    @Nullable
    public T getMax() {
        return this.max;
    }


    @Nullable
    public T getMinSq() {
        return minSq;
    }


    @Nullable
    public T getMaxSq() {
        return maxSq;
    }


    public Tag toNBT() {
        return SerializationHelper.convertToNBT(this.toJson());
    }


    public JsonElement toJson() {
        if(this.isAny()) {
            return JsonNull.INSTANCE;
        }
        else if(this.min != null && this.min.equals(this.max)) {
            return new JsonPrimitive(this.min);
        }
        else {
            JsonObject jsonobject = new JsonObject();
            SerializationHelper.addNullable(this.min, jsonobject, "min", JsonPrimitive::new);
            SerializationHelper.addNullable(this.max, jsonobject, "max", JsonPrimitive::new);

            return jsonobject;
        }
    }


    public abstract void toNetwork(FriendlyByteBuf buf);


    public boolean isAny() {
        return this.min == null && this.max == null;
    }

    @FunctionalInterface
    protected interface MinMaxFactory<T extends Number, R extends MinMax<T>> {

        R create(@Nullable T min, @Nullable T max);
    }

    public static class Doubles extends MinMax<Double> {

        public static final MinMax.Doubles ANY = new MinMax.Doubles((Double) null, (Double) null);


        public static MinMax.Doubles exactly(double pValue) {
            return new MinMax.Doubles(pValue, pValue);
        }


        public static MinMax.Doubles between(double pMin, double pMax) {
            return new MinMax.Doubles(pMin, pMax);
        }


        public static MinMax.Doubles atLeast(double pMin) {
            return new MinMax.Doubles(pMin, (Double) null);
        }


        public static MinMax.Doubles atMost(double pMax) {
            return new MinMax.Doubles((Double) null, pMax);
        }


        public static MinMax.Doubles fromNBT(Tag tag) {
            return fromJson(SerializationHelper.convertToJson(tag));
        }


        public static MinMax.Doubles fromJson(@Nullable JsonElement jsonElement) {
            return fromJson(jsonElement, ANY, JsonElement::getAsDouble, MinMax.Doubles::new);
        }


        public static MinMax.Doubles fromNetwork(FriendlyByteBuf buf) {
            Double min = buf.readNullable(FriendlyByteBuf::readDouble);
            Double max = buf.readNullable(FriendlyByteBuf::readDouble);

            return new Doubles(min, max);
        }


        private Doubles(@Nullable Double minSqr, @Nullable Double maxSqr) {
            super(minSqr, maxSqr);
        }


        @Nullable
        protected Double squareOpt(@Nullable Double pValue) {
            return pValue == null ? null : pValue * pValue;
        }


        public boolean matches(Double value) {
            if(this.min != null && this.min > value) {
                return false;
            }
            else {
                return this.max == null || !(this.max < value);
            }
        }


        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeNullable(this.min, FriendlyByteBuf::writeDouble);
            buf.writeNullable(this.max, FriendlyByteBuf::writeDouble);
        }


        public boolean matchesSqr(double value) {
            if(this.minSq != null && this.minSq > value) {
                return false;
            }
            else {
                return this.maxSq == null || !(this.maxSq < value);
            }
        }
    }

    public static class Ints extends MinMax<Integer> {

        public static final MinMax.Ints ANY = new MinMax.Ints((Integer) null, (Integer) null);

        public static MinMax.Ints exactly(int pValue) {
            return new MinMax.Ints(pValue, pValue);
        }


        public static MinMax.Ints between(int pMin, int pMax) {
            return new MinMax.Ints(pMin, pMax);
        }


        public static MinMax.Ints atLeast(int pMin) {
            return new MinMax.Ints(pMin, (Integer) null);
        }


        public static MinMax.Ints atMost(int pMax) {
            return new MinMax.Ints((Integer) null, pMax);
        }


        public static MinMax.Ints fromNBT(Tag tag) {
            return fromJson(SerializationHelper.convertToJson(tag));
        }


        public static MinMax.Ints fromJson(@Nullable JsonElement jsonElement) {
            return fromJson(jsonElement, ANY, JsonElement::getAsInt, MinMax.Ints::new);
        }


        public static MinMax.Ints fromNetwork(FriendlyByteBuf buf) {
            Integer min = buf.readNullable(FriendlyByteBuf::readInt);
            Integer max = buf.readNullable(FriendlyByteBuf::readInt);

            return new MinMax.Ints(min, max);
        }


        private Ints(@Nullable Integer min, @Nullable Integer max) {
            super(min, max);
        }


        @Nullable
        protected Integer squareOpt(@Nullable Integer value) {
            return value == null ? null : value * value;
        }


        public boolean matches(Integer value) {
            if(this.min != null && this.min > value) {
                return false;
            }
            else {
                return this.max == null || this.max >= value;
            }
        }


        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeNullable(this.min, FriendlyByteBuf::writeInt);
            buf.writeNullable(this.max, FriendlyByteBuf::writeInt);
        }


        public boolean matchesSqr(int value) {
            if(this.minSq != null && this.minSq > value) {
                return false;
            }
            else {
                return this.maxSq == null || this.maxSq >= value;
            }
        }
    }

    public static class Counter implements INbtSerializable, INetworkSerializable, IStatusChecker {

        private final MinMax.Ints minMax;

        private final AtomicInteger current;


        public Counter(MinMax.Ints minMax) {
            this(minMax, new AtomicInteger(0));
        }


        private Counter(MinMax.Ints minMax, AtomicInteger current) {
            this.minMax = minMax;
            this.current = current;
        }


        public static Counter fromNBT(CompoundTag tag) {
            MinMax.Ints minMax = MinMax.Ints.fromNBT(tag.get("minMax"));
            int current = tag.getInt("current");

            return new Counter(minMax, new AtomicInteger(current));
        }


        public static Counter fromNetwork(FriendlyByteBuf buf) {
            MinMax.Ints minMax = MinMax.Ints.fromNetwork(buf);
            int current = buf.readInt();

            return new Counter(minMax, new AtomicInteger(current));
        }


        @Override
        public void reset() {
            this.current.set(0);
        }


        @Override
        public boolean updateAndCheck() {
            if(this.minMax.isAny()) {
                return true;
            }
            else {
                return this.minMax.matches(this.current.getAndIncrement());
            }
        }


        @Override
        public boolean check() {
            return this.minMax.isAny() || this.minMax.matches(this.current.get());
        }


        @Override
        public Component getStatus() {
            if(this.minMax.isAny()) {
                return Component.empty();
            }
            else {
                return Component.literal(" " + this.current.get()).append(this.minMax.getShortDescription());
            }
        }


        @Override
        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();

            tag.put("minMax", this.minMax.toNBT());
            tag.putInt("current", this.current.get());

            return tag;
        }


        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            this.minMax.toNetwork(buf);
            buf.writeInt(this.current.get());
        }
    }


    public static class Timer implements INbtSerializable, INetworkSerializable, IStatusChecker {

        private final MinMax.Doubles minMax;

        private final TickTimer timer;


        public Timer(MinMax.Doubles minMax) {
            this.minMax = minMax;

            float duration;

            if(this.minMax.isAny()) {
                duration = 1;
            }
            else if(this.minMax.max != null) {
                duration = this.minMax.max.floatValue();
            }
            else {
                duration = this.minMax.min.floatValue() + 1f;
            }

            this.timer = new TickTimer(0, duration, false, true, LoopTypes.NO_LOOP);
        }


        private Timer(MinMax.Doubles minMax, TickTimer timer) {
            this.minMax = minMax;
            this.timer = timer;
        }


        public static Timer fromNetwork(FriendlyByteBuf buf) {
            MinMax.Doubles minMax = MinMax.Doubles.fromNetwork(buf);
            TickTimer timer = TickTimer.fromNetwork(buf);

            return new Timer(minMax, timer);
        }


        public static Timer fromNBT(CompoundTag tag) {
            MinMax.Doubles minMax = MinMax.Doubles.fromNBT(tag.get("minMax"));
            TickTimer timer = TickTimer.fromNBT(tag.getCompound("timer"));

            return new Timer(minMax, timer);
        }


        @Override
        public void toNetwork(FriendlyByteBuf buf) {
            this.minMax.toNetwork(buf);
            this.timer.toNetwork(buf);
        }


        @Override
        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();

            tag.put("minMax", this.minMax.toNBT());
            tag.put("timer", this.timer.toNBT());

            return tag;
        }


        @Override
        public void reset() {
            this.timer.stop();
            this.timer.reset();
        }


        @Override
        public boolean updateAndCheck() {
            if(this.minMax.isAny()) {
                return true;
            }
            else {
                this.startIfNeeded();
                this.timer.updateTime();
                return !this.timer.isFinished();
            }
        }


        @Override
        public boolean check() {
            return this.minMax.isAny() || !this.timer.isFinished();
        }


        @Override
        public Component getStatus() {
            if(this.minMax.isAny()) {
                return Component.empty();
            }
            else {
                this.startIfNeeded();
                this.timer.updateTime();
                String timeLeft = DurationFormatUtils.formatDurationHMS(MathHelper.ticksToMillis(this.timer.getTimeLeft()));
                return Component.literal(timeLeft);
            }
        }


        public boolean startIfNeeded() {
            if(!this.minMax.isAny()) {
                this.timer.startIfNeeded();
                return true;
            }

            return false;
        }


        public void stopIfNeeded() {
            this.timer.pause(true);
        }
    }
}