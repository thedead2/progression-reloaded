package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;


public class DamageSourcePredicate implements ITriggerPredicate<DamageSource> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("damage_source");

    public static final DamageSourcePredicate ANY = new DamageSourcePredicate(null, null, null, null, null, EntityPredicate.ANY);

    @Nullable
    private final Boolean isProjectile;

    @Nullable
    private final Boolean isExplosion;

    @Nullable
    private final Boolean isFire;

    @Nullable
    private final Boolean isMagic;

    @Nullable
    private final Boolean isLightning;

    private final EntityPredicate sourceEntity;


    public DamageSourcePredicate(@Nullable Boolean isProjectile, @Nullable Boolean isExplosion, @Nullable Boolean isFire, @Nullable Boolean isMagic, @Nullable Boolean isLightning, EntityPredicate sourceEntity) {
        this.isProjectile = isProjectile;
        this.isExplosion = isExplosion;
        this.isFire = isFire;
        this.isMagic = isMagic;
        this.isLightning = isLightning;
        this.sourceEntity = sourceEntity;
    }


    public static DamageSourcePredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Boolean isProjectile = SerializationHelper.getNullable(jsonObject, "isProjectile", JsonElement::getAsBoolean);
            Boolean isExplosion = SerializationHelper.getNullable(jsonObject, "isExplosion", JsonElement::getAsBoolean);
            Boolean isFire = SerializationHelper.getNullable(jsonObject, "isFire", JsonElement::getAsBoolean);
            Boolean isMagic = SerializationHelper.getNullable(jsonObject, "isMagic", JsonElement::getAsBoolean);
            Boolean isLightning = SerializationHelper.getNullable(jsonObject, "isLightning", JsonElement::getAsBoolean);

            EntityPredicate sourceEntity = EntityPredicate.fromJson(jsonObject.get("sourceEntity"));

            return new DamageSourcePredicate(isProjectile, isExplosion, isFire, isMagic, isLightning, sourceEntity);
        }
        else {
            return ANY;
        }
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
        else if(this.isFire != null && this.isFire != damageSource.isFire()) {
            return false;
        }
        else if(this.isMagic != null && this.isMagic != damageSource.isMagic()) {
            return false;
        }
        else if(this.isLightning != null && this.isLightning != (damageSource == DamageSource.LIGHTNING_BOLT)) {
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
            SerializationHelper.addNullable(this.isProjectile, jsonobject, "isProjectile", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isExplosion, jsonobject, "isExplosion", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isFire, jsonobject, "isFire", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isMagic, jsonobject, "isMagic", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isLightning, jsonobject, "isLightning", JsonPrimitive::new);
            jsonobject.add("sourceEntity", this.sourceEntity.toJson());
            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.empty();
        }
        else {
            if(this.isProjectile != null && this.isProjectile) {
                return Component.literal(" a projectile");
            }
            else if(this.isExplosion != null && this.isExplosion) {
                return Component.literal(" an explosion");
            }
            else if(this.isFire != null && this.isFire) {
                return Component.literal(" fire");
            }
            else if(this.isMagic != null && this.isMagic) {
                return Component.literal(" magic");
            }
            else if(this.isLightning != null && this.isLightning) {
                return Component.literal(" lightning");
            }
            else {
                return this.sourceEntity.getDefaultDescription();
            }
        }
    }
}
