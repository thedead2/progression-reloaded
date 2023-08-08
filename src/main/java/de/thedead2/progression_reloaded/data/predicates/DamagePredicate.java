package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;

public class DamagePredicate implements ITriggerPredicate<DamageSource> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("damage");
    public static final DamagePredicate ANY = new DamagePredicate(MinMax.Doubles.ANY, MinMax.Doubles.ANY, EntityPredicate.ANY, null, DamageSourcePredicate.ANY);
    private final MinMax.Doubles dealtDamage;
    private final MinMax.Doubles takenDamage;
    private final EntityPredicate sourceEntity;
    @Nullable
    private final Boolean blocked;
    private final DamageSourcePredicate damageType;

    public DamagePredicate(MinMax.Doubles pDealtDamage, MinMax.Doubles pTakenDamage, EntityPredicate pSourceEntity, @Nullable Boolean pBlocked, DamageSourcePredicate pType) {
        this.dealtDamage = pDealtDamage;
        this.takenDamage = pTakenDamage;
        this.sourceEntity = pSourceEntity;
        this.blocked = pBlocked;
        this.damageType = pType;
    }

    @Override
    public boolean matches(DamageSource damageSource, Object... addArgs) {
        if (this == ANY) {
            return true;
        } else if (!this.dealtDamage.matches((double) addArgs[1])) {
            return false;
        } else if (!this.takenDamage.matches((double) addArgs[2])) {
            return false;
        } else if (!this.sourceEntity.matches(damageSource.getEntity(), addArgs[0])) {
            return false;
        } else if (this.blocked != null && this.blocked != (boolean) addArgs[3]) {
            return false;
        } else {
            return this.damageType.matches(damageSource);
        }
    }

    public static DamagePredicate fromJson(@Nullable JsonElement pJson) {
        if (pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "damage");
            MinMax.Doubles dealtDamage = MinMax.Doubles.fromJson(jsonobject.get("dealt"));
            MinMax.Doubles takenDamage = MinMax.Doubles.fromJson(jsonobject.get("taken"));
            Boolean blocked = jsonobject.has("blocked") ? GsonHelper.getAsBoolean(jsonobject, "blocked") : null;
            EntityPredicate sourceEntity1 = EntityPredicate.fromJson(jsonobject.get("source_entity"));
            DamageSourcePredicate damageType = DamageSourcePredicate.fromJson(jsonobject.get("damage_type"));
            return new DamagePredicate(dealtDamage, takenDamage, sourceEntity1, blocked, damageType);
        } else {
            return ANY;
        }
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("dealt", this.dealtDamage.serializeToJson());
            jsonobject.add("taken", this.takenDamage.serializeToJson());
            jsonobject.add("source_entity", this.sourceEntity.toJson());
            jsonobject.add("damage_type", this.damageType.toJson());
            if (this.blocked != null) {
                jsonobject.addProperty("blocked", this.blocked);
            }

            return jsonobject;
        }
    }

    public static DamagePredicate from(DamageSource damageSource, Object... addArgs){
        if(damageSource == null) return ANY;
        Double dealtDamage = addArgs[0] != null ? (double) addArgs[0] : null, takenDamage = addArgs[1] != null ? (double) addArgs[1] : null;
        EntityPredicate sourceEntity = EntityPredicate.from(damageSource.getEntity());
        Boolean blocked = addArgs[3] != null ? (boolean) addArgs[3] : null;
        DamageSourcePredicate damageType = DamageSourcePredicate.from(damageSource);
        return new DamagePredicate(dealtDamage != null ? MinMax.Doubles.exactly(dealtDamage) : MinMax.Doubles.ANY, takenDamage != null ? MinMax.Doubles.exactly(takenDamage) : MinMax.Doubles.ANY, sourceEntity, blocked, damageType);
    }
}
