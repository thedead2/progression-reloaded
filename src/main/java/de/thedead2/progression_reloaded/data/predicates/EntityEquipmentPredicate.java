package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;


public class EntityEquipmentPredicate implements ITriggerPredicate<Entity> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("entity_equipment");

    public static final EntityEquipmentPredicate ANY = new EntityEquipmentPredicate(ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);

    private final ItemPredicate head;

    private final ItemPredicate chest;

    private final ItemPredicate legs;

    private final ItemPredicate feet;

    private final ItemPredicate mainHand;

    private final ItemPredicate offhand;


    public EntityEquipmentPredicate(ItemPredicate pHead, ItemPredicate pChest, ItemPredicate pLegs, ItemPredicate pFeet, ItemPredicate pMainhand, ItemPredicate pOffhand) {
        this.head = pHead;
        this.chest = pChest;
        this.legs = pLegs;
        this.feet = pFeet;
        this.mainHand = pMainhand;
        this.offhand = pOffhand;
    }


    public static EntityEquipmentPredicate from(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) {
            return ANY;
        }
        return new EntityEquipmentPredicate(
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.HEAD)),
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.CHEST)),
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.LEGS)),
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.FEET)),
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND)),
                ItemPredicate.from(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND))
        );
    }


    public static EntityEquipmentPredicate fromJson(@Nullable JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "equipment");
            ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("head"));
            ItemPredicate itempredicate1 = ItemPredicate.fromJson(jsonobject.get("chest"));
            ItemPredicate itempredicate2 = ItemPredicate.fromJson(jsonobject.get("legs"));
            ItemPredicate itempredicate3 = ItemPredicate.fromJson(jsonobject.get("feet"));
            ItemPredicate itempredicate4 = ItemPredicate.fromJson(jsonobject.get("mainhand"));
            ItemPredicate itempredicate5 = ItemPredicate.fromJson(jsonobject.get("offhand"));
            return new EntityEquipmentPredicate(
                    itempredicate,
                    itempredicate1,
                    itempredicate2,
                    itempredicate3,
                    itempredicate4,
                    itempredicate5
            );
        }
        else {
            return ANY;
        }
    }


    @Override
    public boolean matches(Entity entity, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else if(!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        else {
            if(!this.head.matches(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
                return false;
            }
            else if(!this.chest.matches(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
                return false;
            }
            else if(!this.legs.matches(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
                return false;
            }
            else if(!this.feet.matches(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
                return false;
            }
            else if(!this.mainHand.matches(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
                return false;
            }
            else {
                return this.offhand.matches(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
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
            jsonobject.add("head", this.head.toJson());
            jsonobject.add("chest", this.chest.toJson());
            jsonobject.add("legs", this.legs.toJson());
            jsonobject.add("feet", this.feet.toJson());
            jsonobject.add("mainhand", this.mainHand.toJson());
            jsonobject.add("offhand", this.offhand.toJson());
            return jsonobject;
        }
    }
}
