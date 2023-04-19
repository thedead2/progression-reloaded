package de.thedead2.progression_reloaded.helpers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.WeakHashMap;

public class StackHelper {
    private static WeakHashMap<Item, String> modiditemcache = new WeakHashMap();
    private static WeakHashMap<Block, String> modidblockcache = new WeakHashMap();

    public static String getModFromItem(Item item) {
        if (modiditemcache.containsKey(item)) return modiditemcache.get(item);
        else {
            String modid = Item.REGISTRY.getNameForObject(item).getResourceDomain();
            modiditemcache.put(item, modid);
            return modid;
        }
    }

    public static String getModFromBlock(Block item) {
        if (modidblockcache.containsKey(item)) return modidblockcache.get(item);
        else {
            String modid = Block.REGISTRY.getNameForObject(item).getResourceDomain();
            modidblockcache.put(item, modid);
            return modid;
        }
    }

    public static ItemStack getStackFromString(String str) {
        if (str == null || str.equals("")) return null;
        return getStackFromArray(str.trim().split(" "));
    }

    public static String getStringFromObject(Object object) {
        if (object instanceof Item) {
            return getStringFromStack(new ItemStack((Item) object));
        } else if (object instanceof Block) {
            return getStringFromStack(new ItemStack((Block) object));
        } else if (object instanceof ItemStack) {
            return getStringFromStack((ItemStack) object);
        } else if (object instanceof String) {
            return (String) object;
        } else if (object instanceof List) {
            return getStringFromStack((ItemStack) ((List) object).get(0));
        } else return "";
    }

    public static String getStringFromStack(ItemStack stack) {
        String str = Item.REGISTRY.getNameForObject(stack.getItem()).toString().replace(" ", "%20");
        if (stack.getHasSubtypes() || stack.isItemStackDamageable()) {
            str = str + " " + stack.getItemDamage();
        }

        if (stack.stackSize > 1) {
            str = str + " *" + stack.stackSize;
        }

        if (stack.hasTagCompound()) {
            str = str + " " + stack.getTagCompound().toString();
        }

        return str;
    }

    public static NBTTagCompound getTag(String[] str, int pos) {
        String s = formatNBT(str, pos).getUnformattedText();
        try {
            NBTBase nbtbase = JsonToNBT.getTagFromJson(s);
            if (!(nbtbase instanceof NBTTagCompound)) return null;
            return (NBTTagCompound) nbtbase;
        } catch (Exception nbtexception) {
            return null;
        }
    }

    public static boolean isMeta(String str) {
        return !isNBT(str) && !isAmount(str);
    }

    public static boolean isNBT(String str) {
        return str.startsWith("{");
    }

    public static boolean isAmount(String str) {
        return str.startsWith("*");
    }

    private static ItemStack getStackFromArray(String[] str) {
        Item item = getItemByText(str[0]);
        if (item == null) return null;

        int meta = 0;
        int amount = 1;
        ItemStack stack = new ItemStack(item, 1, meta);
        NBTTagCompound tag = null;

        for (int i = 1; i <= 3; i++) {
            if (str.length > i) {
                if (isMeta(str[i])) meta = parseMeta(str[i]);
                if (isAmount(str[i])) amount = parseAmount(str[i]);
                if (isNBT(str[i])) tag = getTag(str, i);
            }
        }

        stack.setItemDamage(meta);
        stack.setTagCompound(tag);
        stack.stackSize = amount;
        return stack;
    }

    public static Item getItemByText(String str) {
        str = str.replace("%20", " ");
        Item item = (Item) Item.REGISTRY.getObject(new ResourceLocation(str));
        if (item == null) {
            try {
                Item item1 = Item.getItemById(Integer.parseInt(str));
                item = item1;
            } catch (NumberFormatException numberformatexception) {
                ;
            }
        }

        return item;
    }

    private static ITextComponent formatNBT(String[] str, int start) {
        TextComponentString chatcomponenttext = new TextComponentString("");

        for (int j = start; j < str.length; ++j) {
            if (j > start) {
                chatcomponenttext.appendText(" ");
            }

            Object object = new TextComponentString(str[j]);
            chatcomponenttext.appendSibling((ITextComponent) object);
        }

        return chatcomponenttext;
    }

    private static int parseMeta(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException numberformatexception) {
            return 0;
        }
    }

    private static int parseAmount(String str) {
        try {
            return Integer.parseInt(str.substring(1, str.length()));
        } catch (NumberFormatException numberformatexception) {
            return 0;
        }
    }
}