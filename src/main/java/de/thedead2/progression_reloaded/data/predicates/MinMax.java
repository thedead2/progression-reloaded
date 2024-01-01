package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


public abstract class MinMax<T extends Number> {

    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable(
            "argument.range.empty"));

    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable(
            "argument.range.swapped"));

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


    protected static <T extends Number, R extends MinMax<T>> R fromJson(@Nullable JsonElement jsonElement, R defaultValue, BiFunction<JsonElement, String, T> valueFactory, MinMaxFactory<T, R> minMaxFactory) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            if(GsonHelper.isNumberValue(jsonElement)) {
                T value = valueFactory.apply(jsonElement, "value");
                return minMaxFactory.create(value, value);
            }
            else {
                JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonElement, "value");
                T min = SerializationHelper.getNullable(jsonobject, "min", jsonElement1 -> valueFactory.apply(jsonElement1, "min"));
                T max = SerializationHelper.getNullable(jsonobject, "max", jsonElement1 -> valueFactory.apply(jsonElement1, "max"));
                return minMaxFactory.create(min, max);
            }
        }
        else {
            return defaultValue;
        }
    }


    protected static <T extends Number, R extends MinMax<T>> R fromReader(StringReader pReader, MinMax.BoundsFromReaderFactory<T, R> pBoundedFactory, Function<String, T> pValueFactory, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier, Function<T, T> pFormatter) throws CommandSyntaxException {
        if(!pReader.canRead()) {
            throw ERROR_EMPTY.createWithContext(pReader);
        }
        else {
            int i = pReader.getCursor();

            try {
                T t = optionallyFormat(readNumber(pReader, pValueFactory, pCommandExceptionSupplier), pFormatter);
                T t1;
                if(pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.') {
                    pReader.skip();
                    pReader.skip();
                    t1 = optionallyFormat(readNumber(pReader, pValueFactory, pCommandExceptionSupplier), pFormatter);
                    if(t == null && t1 == null) {
                        throw ERROR_EMPTY.createWithContext(pReader);
                    }
                }
                else {
                    t1 = t;
                }

                if(t == null && t1 == null) {
                    throw ERROR_EMPTY.createWithContext(pReader);
                }
                else {
                    return pBoundedFactory.create(pReader, t, t1);
                }
            }
            catch(CommandSyntaxException commandsyntaxexception) {
                pReader.setCursor(i);
                throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
            }
        }
    }


    @Nullable
    private static <T> T optionallyFormat(@Nullable T pValue, Function<T, T> pFormatter) {
        return pValue == null ? null : pFormatter.apply(pValue);
    }


    @Nullable
    private static <T extends Number> T readNumber(StringReader pReader, Function<String, T> pStringToValueFunction, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier) throws CommandSyntaxException {
        int i = pReader.getCursor();

        while(pReader.canRead() && isAllowedInputChat(pReader)) {
            pReader.skip();
        }

        String s = pReader.getString().substring(i, pReader.getCursor());
        if(s.isEmpty()) {
            return null;
        }
        else {
            try {
                return pStringToValueFunction.apply(s);
            }
            catch(NumberFormatException numberformatexception) {
                throw pCommandExceptionSupplier.get().createWithContext(pReader, s);
            }
        }
    }


    private static boolean isAllowedInputChat(StringReader pReader) {
        char c0 = pReader.peek();
        if((c0 < '0' || c0 > '9') && c0 != '-') {
            if(c0 != '.') {
                return false;
            }
            else {
                return !pReader.canRead(2) || pReader.peek(1) != '.';
            }
        }
        else {
            return true;
        }
    }


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


    public JsonElement serializeToJson() {
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


    public boolean isAny() {
        return this.min == null && this.max == null;
    }


    @FunctionalInterface
    protected interface MinMaxFactory<T extends Number, R extends MinMax<T>> {

        R create(@Nullable T min, @Nullable T max);
    }

    @FunctionalInterface
    protected interface BoundsFromReaderFactory<T extends Number, R extends MinMax<T>> {

        R create(StringReader pReader, @Nullable T pMin, @Nullable T pMax) throws CommandSyntaxException;
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


        public static MinMax.Doubles fromJson(@Nullable JsonElement jsonElement) {
            return fromJson(jsonElement, ANY, GsonHelper::convertToDouble, MinMax.Doubles::new);
        }


        private Doubles(@Nullable Double minSqr, @Nullable Double maxSqr) {
            super(minSqr, maxSqr);
        }


        @Nullable
        protected Double squareOpt(@Nullable Double pValue) {
            return pValue == null ? null : pValue * pValue;
        }


        public static MinMax.Doubles fromReader(StringReader pReader) throws CommandSyntaxException {
            return fromReader(pReader, (p_154807_) -> p_154807_);
        }


        public static MinMax.Doubles fromReader(StringReader pReader, Function<Double, Double> pFormatter) throws CommandSyntaxException {
            return fromReader(pReader, MinMax.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, pFormatter);
        }


        private static MinMax.Doubles create(StringReader p_154796_, @Nullable Double p_154797_, @Nullable Double p_154798_) throws CommandSyntaxException {
            if(p_154797_ != null && p_154798_ != null && p_154797_ > p_154798_) {
                throw ERROR_SWAPPED.createWithContext(p_154796_);
            }
            else {
                return new MinMax.Doubles(p_154797_, p_154798_);
            }
        }


        public boolean matches(double value) {
            if(this.min != null && this.min > value) {
                return false;
            }
            else {
                return this.max == null || !(this.max < value);
            }
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


        public static MinMax.Ints fromJson(@Nullable JsonElement jsonElement) {
            return fromJson(jsonElement, ANY, GsonHelper::convertToInt, MinMax.Ints::new);
        }


        private Ints(@Nullable Integer min, @Nullable Integer max) {
            super(min, max);
        }


        @Nullable
        protected Integer squareOpt(@Nullable Integer value) {
            return value == null ? null : value * value;
        }


        public static MinMax.Ints fromReader(StringReader pReader) throws CommandSyntaxException {
            return fromReader(pReader, (integer) -> integer);
        }


        public static MinMax.Ints fromReader(StringReader pReader, Function<Integer, Integer> pValueFunction) throws CommandSyntaxException {
            return fromReader(pReader, MinMax.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, pValueFunction);
        }


        private static MinMax.Ints create(StringReader p_55378_, @Nullable Integer p_55379_, @Nullable Integer p_55380_) throws CommandSyntaxException {
            if(p_55379_ != null && p_55380_ != null && p_55379_ > p_55380_) {
                throw ERROR_SWAPPED.createWithContext(p_55378_);
            }
            else {
                return new MinMax.Ints(p_55379_, p_55380_);
            }
        }


        public boolean matches(int value) {
            if(this.min != null && this.min > value) {
                return false;
            }
            else {
                return this.max == null || this.max >= value;
            }
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
}