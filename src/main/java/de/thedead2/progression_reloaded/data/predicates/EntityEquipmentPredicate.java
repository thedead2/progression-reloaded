package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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

    private final ItemPredicate mainhand;

    private final ItemPredicate offhand;


    public EntityEquipmentPredicate(ItemPredicate head, ItemPredicate chest, ItemPredicate legs, ItemPredicate feet, ItemPredicate mainhand, ItemPredicate offhand) {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
        this.mainhand = mainhand;
        this.offhand = offhand;
    }


    public static EntityEquipmentPredicate fromJson(@Nullable JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();
            ItemPredicate head = ItemPredicate.fromJson(jsonobject.get("head"));
            ItemPredicate chest = ItemPredicate.fromJson(jsonobject.get("chest"));
            ItemPredicate legs = ItemPredicate.fromJson(jsonobject.get("legs"));
            ItemPredicate feet = ItemPredicate.fromJson(jsonobject.get("feet"));
            ItemPredicate mainHand = ItemPredicate.fromJson(jsonobject.get("mainhand"));
            ItemPredicate offHand = ItemPredicate.fromJson(jsonobject.get("offhand"));

            return new EntityEquipmentPredicate(head, chest, legs, feet, mainHand, offHand);
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
            else if(!this.mainhand.matches(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
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
            jsonobject.add("mainhand", this.mainhand.toJson());
            jsonobject.add("offhand", this.offhand.toJson());
            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" anything");
        }
        else {
            MutableComponent component = Component.empty();

            if(this.head != ItemPredicate.ANY) {
                component.append(" a ").append(this.head.getDefaultDescription());
            }
            if(this.chest != ItemPredicate.ANY) {
                component.append(" a ").append(this.chest.getDefaultDescription());
            }
            if(this.legs != ItemPredicate.ANY) {
                component.append(" a ").append(this.legs.getDefaultDescription());
            }
            if(this.feet != ItemPredicate.ANY) {
                component.append(" a ").append(this.feet.getDefaultDescription());
            }

            if(this.mainhand != ItemPredicate.ANY || this.offhand != ItemPredicate.ANY) {
                component.append(" holding ");

                if(this.mainhand != ItemPredicate.ANY) {
                    component.append(" a ").append(this.mainhand.getDefaultDescription());
                }
                else {
                    component.append(" a ").append(this.offhand.getDefaultDescription()).append(" in the offhand");
                }
            }

            return component;
        }
    }
}
