package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;


public class EntityFlagsPredicate implements ITriggerPredicate<Entity> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("entity_flags");

    public static final EntityFlagsPredicate ANY = new EntityFlagsPredicate(null, null, null, null, null);

    @Nullable
    private final Boolean isOnFire;

    @Nullable
    private final Boolean isSneaking;

    @Nullable
    private final Boolean isSprinting;

    @Nullable
    private final Boolean isSwimming;

    @Nullable
    private final Boolean isBaby;


    public EntityFlagsPredicate(@Nullable Boolean isOnFire, @Nullable Boolean isSneaking, @Nullable Boolean isSprinting, @Nullable Boolean isSwimming, @Nullable Boolean isBaby) {
        this.isOnFire = isOnFire;
        this.isSneaking = isSneaking;
        this.isSprinting = isSprinting;
        this.isSwimming = isSwimming;
        this.isBaby = isBaby;
    }


    public static EntityFlagsPredicate fromJson(@Nullable JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();
            Boolean isOnFire = SerializationHelper.getNullable(jsonobject, "isOnFire", JsonElement::getAsBoolean);
            Boolean isSneaking = SerializationHelper.getNullable(jsonobject, "isSneaking", JsonElement::getAsBoolean);
            Boolean isSprinting = SerializationHelper.getNullable(jsonobject, "isSprinting", JsonElement::getAsBoolean);
            Boolean isSwimming = SerializationHelper.getNullable(jsonobject, "isSwimming", JsonElement::getAsBoolean);
            Boolean isBaby = SerializationHelper.getNullable(jsonobject, "isBaby", JsonElement::getAsBoolean);

            return new EntityFlagsPredicate(isOnFire, isSneaking, isSprinting, isSwimming, isBaby);
        }
        else {
            return ANY;
        }
    }

    public boolean matches(Entity entity, Object... addArgs) {
        if(this.isOnFire != null && entity.isOnFire() != this.isOnFire) {
            return false;
        }
        else if(this.isSneaking != null && entity.isCrouching() != this.isSneaking) {
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
            SerializationHelper.addNullable(this.isOnFire, jsonobject, "isOnFire", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isSneaking, jsonobject, "isSneaking", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isSprinting, jsonobject, "isSprinting", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isSwimming, jsonobject, "isSwimming", JsonPrimitive::new);
            SerializationHelper.addNullable(this.isBaby, jsonobject, "isBaby", JsonPrimitive::new);

            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.empty();
        }
        else {
            MutableComponent component = Component.empty();

            if(this.isOnFire != null && this.isOnFire) {
                component.append(" burning");
            }

            if(this.isSneaking != null && this.isSneaking) {
                component.append(" sneaking");
            }
            else if(this.isSprinting != null && this.isSprinting) {
                component.append(" sprinting");
            }
            else if(this.isSwimming != null && this.isSwimming) {
                component.append(" swimming");
            }

            if(this.isBaby != null && this.isBaby) {
                component.append(" baby");
            }

            component.append(" ");

            return component;
        }
    }
}
