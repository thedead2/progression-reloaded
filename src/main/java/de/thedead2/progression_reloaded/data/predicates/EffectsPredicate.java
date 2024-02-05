package de.thedead2.progression_reloaded.data.predicates;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;


public class EffectsPredicate implements ITriggerPredicate<Map<MobEffect, MobEffectInstance>> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("effects");

    public static final EffectsPredicate ANY = new EffectsPredicate(Collections.emptyMap());

    private final Map<MobEffect, MobEffectInstancePredicate> effects;


    public EffectsPredicate(Map<MobEffect, MobEffectInstancePredicate> effects) {
        this.effects = effects;
    }


    public static EffectsPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonElement, "effects");
            Map<MobEffect, MobEffectInstancePredicate> map = Maps.newLinkedHashMap();

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
                MobEffect mobeffect = ForgeRegistries.MOB_EFFECTS.getValue(resourcelocation);
                MobEffectInstancePredicate mobEffectInstancePredicate = MobEffectInstancePredicate.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()));
                map.put(mobeffect, mobEffectInstancePredicate);
            }

            return new EffectsPredicate(map);
        }
        else {
            return ANY;
        }
    }


    @Override
    public boolean matches(Map<MobEffect, MobEffectInstance> effects, Object... addArgs) {
        if(this != ANY) {
            for(Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
                MobEffectInstance mobeffectinstance = effects.get(entry.getKey());
                if(!entry.getValue().matches(mobeffectinstance)) {
                    return false;
                }
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

            for(Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
                jsonobject.add(ForgeRegistries.MOB_EFFECTS.getKey(entry.getKey()).toString(), entry.getValue().toJson());
            }

            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        return Component.empty();
    }


    public static class MobEffectInstancePredicate implements ITriggerPredicate<MobEffectInstance> {

        public static final ResourceLocation ID = ITriggerPredicate.createId("effect_instance");

        public static final MobEffectInstancePredicate ANY = new MobEffectInstancePredicate(MinMax.Ints.ANY, MinMax.Ints.ANY);

        private final MinMax.Ints amplifier;

        private final MinMax.Ints duration;


        public MobEffectInstancePredicate(MinMax.Ints amplifier, MinMax.Ints duration) {
            this.amplifier = amplifier;
            this.duration = duration;
        }


        public static MobEffectInstancePredicate fromJson(JsonObject jsonObject) {
            MinMax.Ints amplifier = MinMax.Ints.fromJson(jsonObject.get("amplifier"));
            MinMax.Ints duration = MinMax.Ints.fromJson(jsonObject.get("duration"));

            return new MobEffectInstancePredicate(amplifier, duration);
        }


        public boolean matches(@Nullable MobEffectInstance effect, Object... addArgs) {
            if(effect == null) {
                return false;
            }
            else if(!this.amplifier.matches(effect.getAmplifier())) {
                return false;
            }
            else {
                return this.duration.matches(effect.getDuration());
            }
        }


        public JsonElement toJson() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("amplifier", this.amplifier.toJson());
            jsonobject.add("duration", this.duration.toJson());
            return jsonobject;
        }


        @Override
        public Component getDefaultDescription() {
            return Component.empty();
        }
    }
}
