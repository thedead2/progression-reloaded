package de.thedead2.progression_reloaded.data.predicates;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EffectsPredicate implements ITriggerPredicate<Map<MobEffect, MobEffectInstance>> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("effects");
    public static final EffectsPredicate ANY = new EffectsPredicate(Collections.emptyMap());
    private final Map<MobEffect, MobEffectInstancePredicate> effects;

    public EffectsPredicate(Map<MobEffect, MobEffectInstancePredicate> effects) {
        this.effects = effects;
    }

    public static EffectsPredicate from(Map<MobEffect, MobEffectInstance> activeEffectsMap) {
        if(activeEffectsMap == null || activeEffectsMap.isEmpty()) return ANY;
        final Map<MobEffect, MobEffectInstancePredicate> map = new HashMap<>();
        activeEffectsMap.forEach((mobEffect, mobEffectInstance) -> map.put(mobEffect, MobEffectInstancePredicate.from(mobEffectInstance)));
        return new EffectsPredicate(map);
    }

    @Override
    public boolean matches(Map<MobEffect, MobEffectInstance> effects, Object... addArgs) {
        if (this != ANY) {
            for (Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
                MobEffectInstance mobeffectinstance = effects.get(entry.getKey());
                if (!entry.getValue().matches(mobeffectinstance)) {
                    return false;
                }
            }

        }
        return true;
    }

    public static EffectsPredicate fromJson(JsonElement jsonElement){
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonElement, "effects");
            Map<MobEffect, MobEffectInstancePredicate> map = Maps.newLinkedHashMap();

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
                MobEffect mobeffect = ForgeRegistries.MOB_EFFECTS.getValue(resourcelocation);
                MobEffectInstancePredicate mobEffectInstancePredicate = MobEffectInstancePredicate.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()));
                map.put(mobeffect, mobEffectInstancePredicate);
            }

            return new EffectsPredicate(map);
        } else {
            return ANY;
        }
    }

    @Override
    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();

            for(Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
                jsonobject.add(ForgeRegistries.MOB_EFFECTS.getKey(entry.getKey()).toString(), entry.getValue().toJson());
            }

            return jsonobject;
        }
    }

    public static class MobEffectInstancePredicate implements ITriggerPredicate<MobEffectInstance>{
        public static final ResourceLocation ID = ITriggerPredicate.createId("effect_instance");
        public static final MobEffectInstancePredicate ANY = new MobEffectInstancePredicate(MinMax.Ints.ANY, MinMax.Ints.ANY, null, null);
        private final MinMax.Ints amplifier;
        private final MinMax.Ints duration;
        @Nullable
        private final Boolean ambient;
        @Nullable
        private final Boolean visible;

        public MobEffectInstancePredicate(MinMax.Ints pAmplifier, MinMax.Ints pDuration, @Nullable Boolean pAmbient, @Nullable Boolean pVisible) {
            this.amplifier = pAmplifier;
            this.duration = pDuration;
            this.ambient = pAmbient;
            this.visible = pVisible;
        }

        public static MobEffectInstancePredicate from(MobEffectInstance mobEffectInstance) {
            if(mobEffectInstance == null) return ANY;
            return new MobEffectInstancePredicate(MinMax.Ints.exactly(mobEffectInstance.getAmplifier()), MinMax.Ints.exactly(mobEffectInstance.getDuration()), mobEffectInstance.isAmbient(), mobEffectInstance.isVisible());
        }

        public boolean matches(@Nullable MobEffectInstance effect, Object... addArgs) {
            if (effect == null) {
                return false;
            } else if (!this.amplifier.matches(effect.getAmplifier())) {
                return false;
            } else if (!this.duration.matches(effect.getDuration())) {
                return false;
            } else if (this.ambient != null && this.ambient != effect.isAmbient()) {
                return false;
            } else {
                return this.visible == null || this.visible == effect.isVisible();
            }
        }

        public JsonElement toJson() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("amplifier", this.amplifier.serializeToJson());
            jsonobject.add("duration", this.duration.serializeToJson());
            jsonobject.addProperty("ambient", this.ambient);
            jsonobject.addProperty("visible", this.visible);
            return jsonobject;
        }

        public static MobEffectInstancePredicate fromJson(JsonObject pJson) {
            MinMax.Ints amplifier = MinMax.Ints.fromJson(pJson.get("amplifier"));
            MinMax.Ints duration = MinMax.Ints.fromJson(pJson.get("duration"));
            Boolean ambient = pJson.has("ambient") ? GsonHelper.getAsBoolean(pJson, "ambient") : null;
            Boolean visible = pJson.has("visible") ? GsonHelper.getAsBoolean(pJson, "visible") : null;
            return new MobEffectInstancePredicate(amplifier, duration, ambient, visible);
        }
    }
}
