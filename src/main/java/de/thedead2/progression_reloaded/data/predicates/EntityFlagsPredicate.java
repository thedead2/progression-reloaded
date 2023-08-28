package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;


public class EntityFlagsPredicate implements ITriggerPredicate<Entity> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("entity_flags");

    public static final EntityFlagsPredicate ANY = new EntityFlagsPredicate(null, null, null, null, null);

    @Nullable
    private final Boolean isOnFire;

    @Nullable
    private final Boolean isCrouching;

    @Nullable
    private final Boolean isSprinting;

    @Nullable
    private final Boolean isSwimming;

    @Nullable
    private final Boolean isBaby;


    public EntityFlagsPredicate(@Nullable Boolean pIsOnFire, @Nullable Boolean pIsCouching, @Nullable Boolean pIsSprinting, @Nullable Boolean pIsSwimming, @Nullable Boolean pIsBaby) {
        this.isOnFire = pIsOnFire;
        this.isCrouching = pIsCouching;
        this.isSprinting = pIsSprinting;
        this.isSwimming = pIsSwimming;
        this.isBaby = pIsBaby;
    }


    public static EntityFlagsPredicate from(Entity entity) {
        if(entity == null) {
            return ANY;
        }
        return new EntityFlagsPredicate(
                entity.isOnFire(),
                entity.isCrouching(),
                entity.isSprinting(),
                entity.isSwimming(),
                entity instanceof LivingEntity livingEntity ? livingEntity.isBaby() : null
        );
    }


    public static EntityFlagsPredicate fromJson(@Nullable JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "entity flags");
            Boolean isOnFire1 = getOptionalBoolean(jsonobject, "is_on_fire");
            Boolean isSneaking = getOptionalBoolean(jsonobject, "is_sneaking");
            Boolean isSprinting1 = getOptionalBoolean(jsonobject, "is_sprinting");
            Boolean isSwimming1 = getOptionalBoolean(jsonobject, "is_swimming");
            Boolean isBaby1 = getOptionalBoolean(jsonobject, "is_baby");
            return new EntityFlagsPredicate(isOnFire1, isSneaking, isSprinting1, isSwimming1, isBaby1);
        }
        else {
            return ANY;
        }
    }


    @Nullable
    private static Boolean getOptionalBoolean(JsonObject pJson, String pName) {
        return pJson.has(pName) ? GsonHelper.getAsBoolean(pJson, pName) : null;
    }


    public boolean matches(Entity entity, Object... addArgs) {
        if(this.isOnFire != null && entity.isOnFire() != this.isOnFire) {
            return false;
        }
        else if(this.isCrouching != null && entity.isCrouching() != this.isCrouching) {
            return false;
        }
        else if(this.isSprinting != null && entity.isSprinting() != this.isSprinting) {
            return false;
        }
        else if(this.isSwimming != null && entity.isSwimming() != this.isSwimming) {
            return false;
        }
        else {
            return this.isBaby == null || !(entity instanceof LivingEntity livingEntity) || livingEntity.isBaby() == this.isBaby;
        }
    }


    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            this.addOptionalBoolean(jsonobject, "is_on_fire", this.isOnFire);
            this.addOptionalBoolean(jsonobject, "is_sneaking", this.isCrouching);
            this.addOptionalBoolean(jsonobject, "is_sprinting", this.isSprinting);
            this.addOptionalBoolean(jsonobject, "is_swimming", this.isSwimming);
            this.addOptionalBoolean(jsonobject, "is_baby", this.isBaby);
            return jsonobject;
        }
    }


    private void addOptionalBoolean(JsonObject pJson, String pName, @Nullable Boolean pValue) {
        if(pValue != null) {
            pJson.addProperty(pName, pValue);
        }

    }
}
