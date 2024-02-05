package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;


public class EntityPredicate implements ITriggerPredicate<Entity> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("entity");

    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, EffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY);

    private final EntityTypePredicate entityType;

    private final DistancePredicate distanceToPlayer;

    private final LocationPredicate entityLocation;

    private final EffectsPredicate effects;

    private final NbtPredicate nbt;

    private final EntityFlagsPredicate flags;

    private final EntityEquipmentPredicate equipment;

    private final EntityPredicate vehicle;


    public EntityPredicate(EntityTypePredicate entityType, DistancePredicate distanceToPlayer, LocationPredicate entityLocation, EffectsPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment, EntityPredicate vehicle) {
        this.entityType = entityType;
        this.distanceToPlayer = distanceToPlayer;
        this.entityLocation = entityLocation;
        this.effects = effects;
        this.nbt = nbt;
        this.flags = flags;
        this.equipment = equipment;
        this.vehicle = vehicle;
    }


    public EntityPredicate(EntityTypePredicate entityType, DistancePredicate distanceToPlayer, LocationPredicate entityLocation, EffectsPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment) {
        this.entityType = entityType;
        this.distanceToPlayer = distanceToPlayer;
        this.entityLocation = entityLocation;
        this.effects = effects;
        this.nbt = nbt;
        this.flags = flags;
        this.equipment = equipment;
        this.vehicle = this;
    }


    public static EntityPredicate fromJson(@Nullable JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();

            EntityTypePredicate type = EntityTypePredicate.fromJson(jsonobject.get("type"));
            DistancePredicate distanceToPlayer = DistancePredicate.fromJson(jsonobject.get("distance"));
            LocationPredicate entityLocation = LocationPredicate.fromJson(jsonobject.get("entityLocation"));
            EffectsPredicate effects = EffectsPredicate.fromJson(jsonobject.get("effects"));
            NbtPredicate nbt = NbtPredicate.fromJson(jsonobject.get("nbt"));
            EntityFlagsPredicate flags = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
            EntityEquipmentPredicate equipment = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
            EntityPredicate vehicle = fromJson(jsonobject.get("vehicle"));

            return new EntityPredicate(type, distanceToPlayer, entityLocation, effects, nbt, flags, equipment, vehicle);
        }
        else {
            return ANY;
        }
    }


    public static EntityPredicate from(EntityType<?> entityType) {
        return new EntityPredicate(new EntityTypePredicate(entityType, null), DistancePredicate.ANY, LocationPredicate.ANY, EffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY);
    }


    public static CompoundTag getEntityTagToCompare(Entity entity) {
        CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
        if(entity instanceof Player player) {
            ItemStack itemstack = player.getInventory().getSelected();
            if(!itemstack.isEmpty()) {
                compoundtag.put("selectedItem", itemstack.save(new CompoundTag()));
            }
        }

        return compoundtag;
    }


    @Override
    public boolean matches(Entity entity, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else if(entity == null) {
            return false;
        }
        else if(!this.entityType.matches(entity.getType())) {
            return false;
        }
        else {
            PlayerData player = addArgs[0] != null ? (PlayerData) addArgs[0] : null;
            if(player == null) {
                if(this.distanceToPlayer != DistancePredicate.ANY) {
                    return false;
                }
            }
            else {
                Vec3 playerPosition = player.getServerPlayer().position();
                if(!this.distanceToPlayer.matches(new DistancePredicate.DistanceInfo(playerPosition.x, playerPosition.y, playerPosition.z, entity.getX(), entity.getY(), entity.getZ()))) {
                    return false;
                }
            }

            if(!this.entityLocation.matches(new BlockPos(entity.getX(), entity.getY(), entity.getZ()), entity.getLevel())) {
                return false;
            }
            else {
                if(entity instanceof LivingEntity livingEntity && !this.effects.matches(livingEntity.getActiveEffectsMap())) {
                    return false;
                }
                else if(!this.nbt.matches(getEntityTagToCompare(entity))) {
                    return false;
                }
                else if(!this.flags.matches(entity)) {
                    return false;
                }
                else if(!this.equipment.matches(entity)) {
                    return false;
                }
                else {
                    return this.vehicle == this || this.vehicle == ANY || this.vehicle.matches(entity.getVehicle(), addArgs);
                }
            }
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("type", this.entityType.toJson());
            jsonobject.add("distance", this.distanceToPlayer.toJson());
            jsonobject.add("entityLocation", this.entityLocation.toJson());
            jsonobject.add("effects", this.effects.toJson());
            jsonobject.add("nbt", this.nbt.toJson());
            jsonobject.add("flags", this.flags.toJson());
            jsonobject.add("equipment", this.equipment.toJson());
            jsonobject.add("vehicle", this.vehicle != this ? this.vehicle.toJson() : JsonNull.INSTANCE);

            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" entity");
        }
        else {
            MutableComponent component = Component.literal(" a ").append(this.flags.getDefaultDescription()).append(this.entityType.getDefaultDescription());

            if(this.equipment != EntityEquipmentPredicate.ANY) {
                component.append(" wearing ").append(this.equipment.getDefaultDescription());
            }

            if(this.vehicle != this) {
                component.append(" riding ").append(this.vehicle.getDefaultDescription());
            }

            return component;
        }
    }
}
