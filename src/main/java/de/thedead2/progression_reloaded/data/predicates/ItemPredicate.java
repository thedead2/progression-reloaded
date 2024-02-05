package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.*;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ItemPredicate implements ITriggerPredicate<ItemStack> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("item");

    public static final ItemPredicate ANY = new ItemPredicate(null, MinMax.Ints.ANY, Collections.emptySet(), Collections.emptySet(), NbtPredicate.ANY, null);

    private final MinMax.Ints itemDurability;

    private final Item itemType;

    private final Set<EnchantmentPredicate> enchantments;

    private final Set<EnchantmentPredicate> storedEnchantments;

    private final NbtPredicate nbt;

    /**
     * If the item stack is a potion
     **/
    @Nullable
    private final Potion potion;


    public ItemPredicate(Item itemType, MinMax.Ints itemDurability, Set<EnchantmentPredicate> enchantments, Set<EnchantmentPredicate> storedEnchantments, NbtPredicate nbt, @Nullable Potion potion) {
        this.itemType = itemType;
        this.itemDurability = itemDurability;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;
        this.nbt = nbt;
        this.potion = potion;
    }


    public static ItemPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Item itemType = SerializationHelper.getNullable(jsonObject, "item", jsonElement1 -> JsonHelper.itemFromJson(jsonElement1).getItem());
            MinMax.Ints durability = MinMax.Ints.fromJson(jsonObject.get("durability"));

            Set<EnchantmentPredicate> enchantments = new HashSet<>(), storedEnchantments = new HashSet<>();

            JsonArray jsonArray = jsonObject.get("enchantments").getAsJsonArray();
            jsonArray.forEach(jsonElement1 -> enchantments.add(EnchantmentPredicate.fromJson(jsonElement1)));
            JsonArray jsonArray1 = jsonObject.get("storedEnchantments").getAsJsonArray();
            jsonArray1.forEach(jsonElement1 -> storedEnchantments.add(EnchantmentPredicate.fromJson(jsonElement1)));

            NbtPredicate nbt = NbtPredicate.fromJson(jsonObject.get("nbt"));

            Potion potion = SerializationHelper.getNullable(jsonObject, "potion", jsonElement1 -> ForgeRegistries.POTIONS.getValue(new ResourceLocation(jsonElement1.getAsString())));

            return new ItemPredicate(itemType, durability, enchantments, storedEnchantments, nbt, potion);
        }
        else {
            return ANY;
        }
    }


    public static ItemPredicate from(Item item) {
        return new ItemPredicate(item, MinMax.Ints.ANY, Collections.emptySet(), Collections.emptySet(), NbtPredicate.ANY, null);
    }

    @Override
    public boolean matches(ItemStack itemStack, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else if(this.itemType != null && !this.itemType.equals(itemStack.getItem())) {
            return false;
        }
        else if(!this.itemDurability.isAny() && !itemStack.isDamageableItem()) {
            return false;
        }
        else if(!this.itemDurability.matches(itemStack.getMaxDamage() - itemStack.getDamageValue())) {
            return false;
        }
        else if(!this.nbt.matches(itemStack.getTag())) {
            return false;
        }
        else {
            if(!this.enchantments.isEmpty()) {
                Map<Enchantment, Integer> map = itemStack.getAllEnchantments();

                for(EnchantmentPredicate enchantmentpredicate : this.enchantments) {
                    if(!enchantmentpredicate.matches(map)) {
                        return false;
                    }
                }
            }

            if(!this.storedEnchantments.isEmpty()) {
                Map<Enchantment, Integer> map1 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));

                for(EnchantmentPredicate storedEnchantment : this.storedEnchantments) {
                    if(!storedEnchantment.matches(map1)) {
                        return false;
                    }
                }
            }

            Potion potion = PotionUtils.getPotion(itemStack);
            return this.potion == null || this.potion == potion;
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonObject = new JsonObject();
            SerializationHelper.addNullable(this.itemType, jsonObject, "item", item -> JsonHelper.itemToJson(item.getDefaultInstance()));

            jsonObject.add("durability", this.itemDurability.toJson());

            if(!this.enchantments.isEmpty()) {
                jsonObject.add("enchantments", CollectionHelper.saveToJson(this.enchantments, EnchantmentPredicate::toJson));
            }

            if(!this.storedEnchantments.isEmpty()) {
                jsonObject.add("stored_enchantments", CollectionHelper.saveToJson(this.storedEnchantments, EnchantmentPredicate::toJson));
            }

            jsonObject.add("nbt", this.nbt.toJson());

            SerializationHelper.addNullable(this.potion, jsonObject, "potion", potion1 -> new JsonPrimitive(ForgeRegistries.POTIONS.getKey(potion1).toString()));

            return jsonObject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" anything");
        }
        else {
            if(this.itemType != null) {
                return this.itemType.getDescription();
            }
            else {
                return Component.literal(this.potion.getName(""));
            }
        }
    }
}
