package de.thedead2.progression_reloaded.data.predicates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class StatePropertiesPredicate<S extends StateHolder<?, S>> implements ITriggerPredicate<S> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("state_properties");

    public static final StatePropertiesPredicate<? extends StateHolder<?, ?>> ANY = new StatePropertiesPredicate<>(
            ImmutableList.of());

    private final List<PropertyMatcher<S>> properties;


    StatePropertiesPredicate(List<PropertyMatcher<S>> properties) {
        this.properties = ImmutableList.copyOf(properties);
    }


    public static <S extends StateHolder<?, S>> StatePropertiesPredicate<S> from(S state) {
        final List<PropertyMatcher<S>> properties = new ArrayList<>();
        var prop = state.getProperties();
        prop.forEach(property -> properties.add(PropertyMatcher.from(property.getName(), state, property)));
        return new StatePropertiesPredicate<>(properties);
    }


    @SuppressWarnings("unchecked")
    public static <S extends StateHolder<?, S>> StatePropertiesPredicate<S> fromJson(@Nullable JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "properties");
            List<PropertyMatcher<S>> list = Lists.newArrayList();

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                list.add(PropertyMatcher.fromJson(entry.getKey(), entry.getValue()));
            }

            return new StatePropertiesPredicate<>(list);
        }
        else {
            return (StatePropertiesPredicate<S>) ANY;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(S stateHolder, Object... addArgs) {
        for(PropertyMatcher<S> propertyMatcher : this.properties) {
            if(!propertyMatcher.match((StateDefinition<?, S>) addArgs[0], stateHolder)) {
                return false;
            }
        }

        return true;
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            if(!this.properties.isEmpty()) {
                this.properties.forEach((propertyMatcher) -> jsonobject.add(propertyMatcher.getName(), propertyMatcher.toJson()));
            }

            return jsonobject;
        }
    }


    abstract static class PropertyMatcher<S extends StateHolder<?, S>> {

        private final String name;


        public PropertyMatcher(String pName) {
            this.name = pName;
        }


        public static <S extends StateHolder<?, S>, T extends Comparable<T>> PropertyMatcher<S> from(String name, StateHolder<?, S> properties, Property<T> property) {
            return ExactPropertyMatcher.from(name, properties, property);
        }


        private static <S extends StateHolder<?, S>> PropertyMatcher<S> fromJson(String name, JsonElement pJson) {
            if(pJson.isJsonPrimitive()) {
                String value = pJson.getAsString();

                return new ExactPropertyMatcher<>(name, value);
            }
            else {
                JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "value");
                String min = jsonobject.has("min") ? getStringOrNull(jsonobject.get("min")) : null;
                String max = jsonobject.has("max") ? getStringOrNull(jsonobject.get("max")) : null;

                return min != null && min.equals(max) ? new ExactPropertyMatcher<>(name, min) : new RangedPropertyMatcher<>(name, min, max);
            }
        }


        @Nullable
        private static String getStringOrNull(JsonElement pJson) {
            return pJson.isJsonNull() ? null : pJson.getAsString();
        }


        public boolean match(StateDefinition<?, S> properties, S propertyToMatch) {
            Property<?> property = properties.getProperty(this.name);
            return property != null && this.match(propertyToMatch, property);
        }


        protected abstract <T extends Comparable<T>> boolean match(StateHolder<?, S> properties, Property<T> property);

        public abstract JsonElement toJson();


        public String getName() {
            return this.name;
        }

    }

    static class ExactPropertyMatcher<S extends StateHolder<?, S>> extends PropertyMatcher<S> {

        private final String value;


        public ExactPropertyMatcher(String name, String value) {
            super(name);
            this.value = value;
        }


        public static <S extends StateHolder<?, S>, T extends Comparable<T>> ExactPropertyMatcher<S> from(String name, StateHolder<?, S> properties, Property<T> propertyTarget) {
            String name1 = propertyTarget.getName();
            T t = properties.getValue(propertyTarget);
            Optional<T> optional = propertyTarget.getValue(name1);
            ModHelper.LOGGER.debug("State properties are the same: " + t.equals(optional.orElseThrow()));
            return new ExactPropertyMatcher<>(name, name1);
        }


        protected <T extends Comparable<T>> boolean match(StateHolder<?, S> properties, Property<T> propertyTarget) {
            T t = properties.getValue(propertyTarget);
            Optional<T> optional = propertyTarget.getValue(this.value);
            return optional.isPresent() && t.compareTo(optional.get()) == 0;
        }


        public JsonElement toJson() {
            return new JsonPrimitive(this.value);
        }
    }

    static class RangedPropertyMatcher<S extends StateHolder<?, S>> extends PropertyMatcher<S> {

        @Nullable
        private final String minValue;

        @Nullable
        private final String maxValue;


        public RangedPropertyMatcher(String name, @Nullable String min, @Nullable String max) {
            super(name);
            this.minValue = min;
            this.maxValue = max;
        }


        protected <T extends Comparable<T>> boolean match(StateHolder<?, S> properties, Property<T> propertyTarget) {
            T t = properties.getValue(propertyTarget);
            if(this.minValue != null) {
                Optional<T> optional = propertyTarget.getValue(this.minValue);
                if(optional.isEmpty() || t.compareTo(optional.get()) < 0) {
                    return false;
                }
            }

            if(this.maxValue != null) {
                Optional<T> optional1 = propertyTarget.getValue(this.maxValue);
                return optional1.isPresent() && t.compareTo(optional1.get()) <= 0;
            }

            return true;
        }


        public JsonElement toJson() {
            JsonObject jsonobject = new JsonObject();
            if(this.minValue != null) {
                jsonobject.addProperty("min", this.minValue);
            }

            if(this.maxValue != null) {
                jsonobject.addProperty("max", this.maxValue);
            }

            return jsonobject;
        }
    }
}
