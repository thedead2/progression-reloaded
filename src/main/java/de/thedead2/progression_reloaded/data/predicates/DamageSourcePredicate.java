package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;


public class DamageSourcePredicate implements ITriggerPredicate<DamageSource> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("damage_source");

    public static final DamageSourcePredicate ANY = new DamageSourcePredicate(null, null, null, null, null, null, null, null, EntityPredicate.ANY, EntityPredicate.ANY);

    @Nullable
    private final Boolean isProjectile;

    @Nullable
    private final Boolean isExplosion;

    @Nullable
    private final Boolean bypassesArmor;

    @Nullable
    private final Boolean bypassesInvulnerability;

    @Nullable
    private final Boolean bypassesMagic;

    @Nullable
    private final Boolean isFire;

    @Nullable
    private final Boolean isMagic;

    @Nullable
    private final Boolean isLightning;

    private final EntityPredicate directEntity;

    private final EntityPredicate sourceEntity;


    public DamageSourcePredicate(@Nullable Boolean isProjectile, @Nullable Boolean isExplosion, @Nullable Boolean bypassesArmor, @Nullable Boolean bypassesInvulnerability, @Nullable Boolean bypassesMagic, @Nullable Boolean isFire, @Nullable Boolean isMagic, @Nullable Boolean isLightning, EntityPredicate directEntity, EntityPredicate sourceEntity) {
        this.isProjectile = isProjectile;
        this.isExplosion = isExplosion;
        this.bypassesArmor = bypassesArmor;
        this.bypassesInvulnerability = bypassesInvulnerability;
        this.bypassesMagic = bypassesMagic;
        this.isFire = isFire;
        this.isMagic = isMagic;
        this.isLightning = isLightning;
        this.directEntity = directEntity;
        this.sourceEntity = sourceEntity;
    }


    public static DamageSourcePredicate from(DamageSource damageSource) {
        if(damageSource == null) {
            return ANY;
        }
        boolean isProjectile, isExplosion, bypassesArmor, bypassesInvulnerability, bypassesMagic, isFire, isMagic, isLightning;
        isProjectile = damageSource.isProjectile();
        isExplosion = damageSource.isExplosion();
        bypassesArmor = damageSource.isBypassArmor();
        bypassesInvulnerability = damageSource.isBypassInvul();
        bypassesMagic = damageSource.isBypassMagic();
        isFire = damageSource.isFire();
        isMagic = damageSource.isMagic();
        isLightning = (damageSource == DamageSource.LIGHTNING_BOLT);
        EntityPredicate directEntity, sourceEntity;
        directEntity = EntityPredicate.from(damageSource.getDirectEntity());
        sourceEntity = EntityPredicate.from(damageSource.getEntity());
        return new DamageSourcePredicate(
                isProjectile,
                isExplosion,
                bypassesArmor,
                bypassesInvulnerability,
                bypassesMagic,
                isFire,
                isMagic,
                isLightning,
                directEntity,
                sourceEntity
        );
    }


    public static DamageSourcePredicate fromJson(JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "damage type");
            Boolean isProjectile1 = getOptionalBoolean(jsonobject, "is_projectile");
            Boolean isExplosion1 = getOptionalBoolean(jsonobject, "is_explosion");
            Boolean bypassesArmor1 = getOptionalBoolean(jsonobject, "bypasses_armor");
            Boolean bypasses_invulnerability = getOptionalBoolean(jsonobject, "bypasses_invulnerability");
            Boolean obool4 = getOptionalBoolean(jsonobject, "bypasses_magic");
            Boolean obool5 = getOptionalBoolean(jsonobject, "is_fire");
            Boolean obool6 = getOptionalBoolean(jsonobject, "is_magic");
            Boolean obool7 = getOptionalBoolean(jsonobject, "is_lightning");
            EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("direct_entity"));
            EntityPredicate entitypredicate1 = EntityPredicate.fromJson(jsonobject.get("source_entity"));
            return new DamageSourcePredicate(
                    isProjectile1,
                    isExplosion1,
                    bypassesArmor1,
                    bypasses_invulnerability,
                    obool4,
                    obool5,
                    obool6,
                    obool7,
                    entitypredicate,
                    entitypredicate1
            );
        }
        else {
            return ANY;
        }
    }


    @Nullable
    private static Boolean getOptionalBoolean(JsonObject pJson, String pProperty) {
        return pJson.has(pProperty) ? GsonHelper.getAsBoolean(pJson, pProperty) : null;
    }


    @Override
    public boolean matches(DamageSource damageSource, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else if(this.isProjectile != null && this.isProjectile != damageSource.isProjectile()) {
            return false;
        }
        else if(this.isExplosion != null && this.isExplosion != damageSource.isExplosion()) {
            return false;
        }
        else if(this.bypassesArmor != null && this.bypassesArmor != damageSource.isBypassArmor()) {
            return false;
        }
        else if(this.bypassesInvulnerability != null && this.bypassesInvulnerability != damageSource.isBypassInvul()) {
            return false;
        }
        else if(this.bypassesMagic != null && this.bypassesMagic != damageSource.isBypassMagic()) {
            return false;
        }
        else if(this.isFire != null && this.isFire != damageSource.isFire()) {
            return false;
        }
        else if(this.isMagic != null && this.isMagic != damageSource.isMagic()) {
            return false;
        }
        else if(this.isLightning != null && this.isLightning != (damageSource == DamageSource.LIGHTNING_BOLT)) {
            return false;
        }
        else if(!this.directEntity.matches(damageSource.getDirectEntity(), addArgs[0])) {
            return false;
        }
        else {
            return this.sourceEntity.matches(damageSource.getEntity(), addArgs[0]);
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            this.addOptionally(jsonobject, "is_projectile", this.isProjectile);
            this.addOptionally(jsonobject, "is_explosion", this.isExplosion);
            this.addOptionally(jsonobject, "bypasses_armor", this.bypassesArmor);
            this.addOptionally(jsonobject, "bypasses_invulnerability", this.bypassesInvulnerability);
            this.addOptionally(jsonobject, "bypasses_magic", this.bypassesMagic);
            this.addOptionally(jsonobject, "is_fire", this.isFire);
            this.addOptionally(jsonobject, "is_magic", this.isMagic);
            this.addOptionally(jsonobject, "is_lightning", this.isLightning);
            jsonobject.add("direct_entity", this.directEntity.toJson());
            jsonobject.add("source_entity", this.sourceEntity.toJson());
            return jsonobject;
        }
    }


    private void addOptionally(JsonObject pJson, String pProperty, @Nullable Boolean pValue) {
        if(pValue != null) {
            pJson.addProperty(pProperty, pValue);
        }

    }
}
