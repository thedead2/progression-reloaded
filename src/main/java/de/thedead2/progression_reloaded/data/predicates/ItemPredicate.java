package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class ItemPredicate implements ITriggerPredicate<ItemStack> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("item");
    private final ItemStack item;
    private final MinMaxNumber<Integer> itemCount;
    private final MinMaxNumber<Integer> itemDamage;
    private final EnchantmentPredicate enchantments;
    private final NbtPredicate nbt;

    @SuppressWarnings("unchecked")
    public static final ItemPredicate ANY = new ItemPredicate(null, (MinMaxNumber<Integer>) MinMaxNumber.ANY, (MinMaxNumber<Integer>) MinMaxNumber.ANY, EnchantmentPredicate.ANY, NbtPredicate.ANY);

    public ItemPredicate(ItemStack item, MinMaxNumber<Integer> itemCount, MinMaxNumber<Integer> itemDamage, EnchantmentPredicate enchantments, NbtPredicate nbt){
        this.item = item;
        this.itemCount = itemCount;
        this.itemDamage = itemDamage;
        this.enchantments = enchantments;
        this.nbt = nbt;
    }

    public static ItemPredicate fromJson(JsonElement item) {
        return null;
    }

    @Override
    public boolean matches(ItemStack itemStack, Object... addArgs) {
        if(this == ANY) return true;
        int itemCount, itemDamage;
        EnchantmentPredicate enchantments;
        CompoundTag nbt;

        if(addArgs != null){
            itemCount = (int) addArgs[0];
            itemDamage = (int) addArgs[1];
            enchantments = (EnchantmentPredicate) addArgs[2];
            nbt = (CompoundTag) addArgs[3];
        }
        else {
            itemCount = itemStack.getCount();
            itemDamage = itemStack.getDamageValue();
            enchantments = EnchantmentPredicate.fromSet(itemStack.getAllEnchantments().keySet());
            nbt = itemStack.getTag();
        }
        return this.item.equals(itemStack, true) && this.itemCount.isInRange(itemCount) && this.itemDamage.isInRange(itemDamage) && this.enchantments.matches(enchantments) && this.nbt.matches(nbt);
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public Builder<ItemPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<ItemStack> copy() {
        return null;
    }

    public static ItemPredicate from(ItemStack itemStack) {
        return new ItemPredicate(itemStack, new MinMaxNumber<>(itemStack.getCount()), new MinMaxNumber<>(itemStack.getDamageValue()), EnchantmentPredicate.fromSet(itemStack.getAllEnchantments().keySet()), NbtPredicate.from(itemStack.getTag()));
    }
}
