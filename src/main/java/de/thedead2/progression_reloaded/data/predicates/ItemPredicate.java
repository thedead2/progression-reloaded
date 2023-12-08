package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
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

    public static final ItemPredicate ANY = new ItemPredicate(null, MinMax.Ints.ANY, MinMax.Ints.ANY, Collections.emptySet(), Collections.emptySet(), NbtPredicate.ANY, null);

    private final MinMax.Ints itemCount;

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


    public ItemPredicate(Item itemType, MinMax.Ints itemCount, MinMax.Ints itemDurability, Set<EnchantmentPredicate> enchantments, Set<EnchantmentPredicate> storedEnchantments, NbtPredicate nbt, @Nullable Potion potion) {
        this.itemType = itemType;
        this.itemCount = itemCount;
        this.itemDurability = itemDurability;
        this.enchantments = enchantments;
        this.storedEnchantments = storedEnchantments;
        this.nbt = nbt;
        this.potion = potion;
    }


    public static ItemPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "item");
            Item itemType = jsonObject.has("item") ? JsonHelper.itemFromJson(jsonObject.get("item").getAsJsonObject()).getItem() : null;
            MinMax.Ints count = MinMax.Ints.fromJson(jsonObject.get("count"));
            MinMax.Ints durability = MinMax.Ints.fromJson(jsonObject.get("durability"));

            NbtPredicate nbt = NbtPredicate.fromJson(jsonObject.get("nbt"));

            Potion potion = null;
            if(jsonObject.has("potion")) {
                potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion")));
            }

            Set<EnchantmentPredicate> enchantments = new HashSet<>(), storedEnchantments = new HashSet<>();

            JsonArray jsonArray = jsonObject.get("enchantments").getAsJsonArray();
            jsonArray.forEach(jsonElement1 -> enchantments.add(EnchantmentPredicate.fromJson(jsonElement1)));
            JsonArray jsonArray1 = jsonObject.get("stored_enchantments").getAsJsonArray();
            jsonArray1.forEach(jsonElement1 -> storedEnchantments.add(EnchantmentPredicate.fromJson(jsonElement1)));
            return new ItemPredicate(itemType, count, durability, enchantments, storedEnchantments, nbt, potion);
        }
        else {
            return ANY;
        }
    }


    public static ItemPredicate from(ItemStack itemStack) {
        int count = itemStack.getCount();
        int damage = itemStack.getMaxDamage() - itemStack.getDamageValue();
        Map<Enchantment, Integer> map = itemStack.getAllEnchantments();
        Set<EnchantmentPredicate> enchantments = new HashSet<>();
        map.keySet().forEach(enchantment -> enchantments.add(EnchantmentPredicate.from(enchantment)));
        Map<Enchantment, Integer> map1 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));
        Set<EnchantmentPredicate> storedEnchantments = new HashSet<>();
        map1.keySet().forEach(enchantment -> storedEnchantments.add(EnchantmentPredicate.from(enchantment)));
        NbtPredicate nbt = NbtPredicate.from(itemStack.getTag());
        Potion potion = PotionUtils.getPotion(itemStack);

        return new ItemPredicate(
                itemStack.getItem(),
                MinMax.Ints.exactly(count),
                itemStack.isDamageableItem() ? MinMax.Ints.exactly(damage) : MinMax.Ints.ANY,
                enchantments,
                storedEnchantments,
                nbt,
                potion
        );
    }


    @Override
    public boolean matches(ItemStack itemStack, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else if(this.itemType != null && !this.itemType.equals(itemStack.getItem())) {
            return false;
        }
        else if(!this.itemCount.matches(itemStack.getCount())) {
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
            if(this.itemType != null) {
                jsonObject.add("item", JsonHelper.itemToJson(this.itemType.getDefaultInstance()));
            }
            jsonObject.add("count", this.itemCount.serializeToJson());
            jsonObject.add("durability", this.itemCount.serializeToJson());
            jsonObject.add("nbt", this.nbt.toJson());
            if(!this.enchantments.isEmpty()) {
                JsonArray jsonArray = new JsonArray();

                for(EnchantmentPredicate enchantmentPredicate : this.enchantments) {
                    jsonArray.add(enchantmentPredicate.toJson());
                }

                jsonObject.add("enchantments", jsonArray);
            }

            if(!this.storedEnchantments.isEmpty()) {
                JsonArray jsonArray = new JsonArray();

                for(EnchantmentPredicate enchantmentPredicate : this.storedEnchantments) {
                    jsonArray.add(enchantmentPredicate.toJson());
                }

                jsonObject.add("stored_enchantments", jsonArray);
            }

            if(this.potion != null) {
                jsonObject.addProperty("potion", ForgeRegistries.POTIONS.getKey(this.potion).toString());
            }

            return jsonObject;
        }
    }
}
