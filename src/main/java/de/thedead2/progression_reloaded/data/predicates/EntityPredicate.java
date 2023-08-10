package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class EntityPredicate implements ITriggerPredicate<Entity> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("entity");
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, EffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY);

    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final LocationPredicate steppingOnLocation;
    private final EffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final EntityPredicate vehicle;
    private final EntityPredicate passenger;
    private final EntityPredicate targetedEntity;

    public EntityPredicate(EntityTypePredicate entityType, DistancePredicate distanceToPlayer, LocationPredicate location, LocationPredicate steppingOnLocation, EffectsPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment, EntityPredicate vehicle, EntityPredicate passenger, EntityPredicate targetedEntity) {
        this.entityType = entityType;
        this.distanceToPlayer = distanceToPlayer;
        this.location = location;
        this.steppingOnLocation = steppingOnLocation;
        this.effects = effects;
        this.nbt = nbt;
        this.flags = flags;
        this.equipment = equipment;
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.targetedEntity = targetedEntity;
    }

    public EntityPredicate(EntityTypePredicate entityType, DistancePredicate distanceToPlayer, LocationPredicate location, LocationPredicate steppingOnLocation, EffectsPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment) {
        this.entityType = entityType;
        this.distanceToPlayer = distanceToPlayer;
        this.location = location;
        this.steppingOnLocation = steppingOnLocation;
        this.effects = effects;
        this.nbt = nbt;
        this.flags = flags;
        this.equipment = equipment;
        this.passenger = this;
        this.vehicle = this;
        this.targetedEntity = this;
    }

    public static EntityPredicate from(Entity entity) {
        if(entity == null) return ANY;
        EntityTypePredicate entityType = EntityTypePredicate.from(entity.getType());
        LocationPredicate location = LocationPredicate.from(new BlockPos(entity.getX(), entity.getY(), entity.getZ()), entity.getLevel());
        LocationPredicate steppingOnLocation = LocationPredicate.from(new BlockPos(Vec3.atCenterOf(entity.getOnPos())), entity.getLevel());
        if(entity instanceof LivingEntity livingEntity) {
            EffectsPredicate effects = EffectsPredicate.from(livingEntity.getActiveEffectsMap());
            NbtPredicate nbt = NbtPredicate.from(NbtPredicate.getEntityTagToCompare(entity));
            EntityFlagsPredicate flags = EntityFlagsPredicate.from(entity);
            EntityEquipmentPredicate equipment = EntityEquipmentPredicate.from(entity);
            EntityPredicate vehicle = EntityPredicate.from(entity.getVehicle());
            EntityPredicate passenger = EntityPredicate.from(entity.getPassengers().stream().findFirst().orElse(null));
            EntityPredicate targetedEntity = entity instanceof Mob mob ? EntityPredicate.from(mob.getTarget()) : null;

            return new EntityPredicate(entityType, DistancePredicate.ANY, location, steppingOnLocation, effects, nbt, flags, equipment, vehicle, passenger, targetedEntity);
        }
        return new EntityPredicate(entityType, DistancePredicate.ANY, location, steppingOnLocation ,EffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY);
    }

    @Override
    public boolean matches(Entity entity, Object... addArgs) {
        if (this == ANY) {
            return true;
        } else if (entity == null) {
            return false;
        } else if (!this.entityType.matches(entity.getType())) {
            return false;
        } else {
            SinglePlayer player = addArgs[0] != null ? (SinglePlayer) addArgs[0] : null;
            if (player == null) {
                if (this.distanceToPlayer != DistancePredicate.ANY) {
                    return false;
                }
            } else {
                Vec3 playerPosition = player.getServerPlayer().position();
                if (!this.distanceToPlayer.matches(new DistancePredicate.DistanceInfo(playerPosition.x, playerPosition.y, playerPosition.z, entity.getX(), entity.getY(), entity.getZ()))) {
                    return false;
                }
            }

            if (!this.location.matches(new BlockPos(entity.getX(), entity.getY(), entity.getZ()), entity.getLevel())) {
                return false;
            } else {
                if (this.steppingOnLocation != LocationPredicate.ANY) {
                    Vec3 vec3 = Vec3.atCenterOf(entity.getOnPos());
                    if (!this.steppingOnLocation.matches(new BlockPos(vec3), entity.getLevel())) {
                        return false;
                    }
                }

                if (entity instanceof LivingEntity livingEntity && !this.effects.matches(livingEntity.getActiveEffectsMap())) {
                    return false;
                } else if (!this.nbt.matches(NbtPredicate.getEntityTagToCompare(entity))) {
                    return false;
                } else if (!this.flags.matches(entity)) {
                    return false;
                } else if (!this.equipment.matches(entity)) {
                    return false;
                } else if (this.vehicle != this && this.vehicle != ANY && !this.vehicle.matches(entity.getVehicle(), addArgs)) {
                    return false;
                } else if (this.passenger != ANY && this.passenger != this && entity.getPassengers().stream().noneMatch((entity1) -> this.passenger.matches(entity1, addArgs))) {
                    return false;
                } else if(this.targetedEntity != this && this.targetedEntity != ANY && !this.targetedEntity.matches(entity instanceof Mob mob ? mob.getTarget() : null)){
                    return false;
                }
                else return true;
            }
        }
    }

    public static EntityPredicate fromJson(@Nullable JsonElement pJson) {
        if (pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "entity");
            EntityTypePredicate type = EntityTypePredicate.fromJson(jsonobject.get("type"));
            DistancePredicate distanceToPlayer = DistancePredicate.fromJson(jsonobject.get("distance"));
            LocationPredicate location = LocationPredicate.fromJson(jsonobject.get("location"));
            LocationPredicate steppingOn = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
            EffectsPredicate effects1 = EffectsPredicate.fromJson(jsonobject.get("effects"));
            NbtPredicate nbt = NbtPredicate.fromJson(jsonobject.get("nbt"));
            EntityFlagsPredicate flags1 = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
            EntityEquipmentPredicate equipment1 = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
            EntityPredicate vehicle1 = fromJson(jsonobject.get("vehicle"));
            EntityPredicate passenger1 = fromJson(jsonobject.get("passenger"));
            EntityPredicate targetedEntity1 = fromJson(jsonobject.get("targeted_entity"));
            return new EntityPredicate(type, distanceToPlayer, location, steppingOn, effects1, nbt, flags1, equipment1, vehicle1, passenger1, targetedEntity1);
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
            jsonobject.add("type", this.entityType.toJson());
            jsonobject.add("distance", this.distanceToPlayer.toJson());
            jsonobject.add("location", this.location.toJson());
            jsonobject.add("stepping_on", this.steppingOnLocation.toJson());
            jsonobject.add("effects", this.effects.toJson());
            jsonobject.add("nbt", this.nbt.toJson());
            jsonobject.add("flags", this.flags.toJson());
            jsonobject.add("equipment", this.equipment.toJson());
            jsonobject.add("vehicle", this.vehicle != this ? this.vehicle.toJson() : JsonNull.INSTANCE);
            jsonobject.add("passenger", this.passenger != this ? this.passenger.toJson() : JsonNull.INSTANCE);
            jsonobject.add("targeted_entity", this.targetedEntity != this ? this.targetedEntity.toJson() : JsonNull.INSTANCE);
            return jsonobject;
        }
    }
}
