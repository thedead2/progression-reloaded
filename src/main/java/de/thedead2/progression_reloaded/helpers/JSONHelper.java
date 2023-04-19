package de.thedead2.progression_reloaded.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.criteria.*;
import de.thedead2.progression_reloaded.api.special.IEnum;
import de.thedead2.progression_reloaded.api.special.ISpecialJSON;
import de.thedead2.progression_reloaded.handlers.APIHandler;
import de.thedead2.progression_reloaded.handlers.RuleHandler;
import de.thedead2.progression_reloaded.lib.PInfo;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JSONHelper {
    public static Enum getEnum(JsonObject data, String string, Enum default_) {
        if (data.get("enum:" + string) != null) {
            try {
                return Enum.valueOf((Class)default_.getClass(), getString(data, "enum:" + string, default_.name()));
            } catch (Exception e) { e.printStackTrace(); }
        }

        return default_;
    }

    public static boolean getBoolean(JsonObject data, String string, boolean default_) {
        if (data.get(string) != null) {
            return data.get(string).getAsBoolean();
        }

        return default_;
    }

    public static int getInteger(JsonObject data, String string, int default_) {
        if (data.get(string) != null) {
            return data.get(string).getAsInt();
        }

        return default_;
    }

    public static float getFloat(JsonObject data, String string, float default_) {
        if (data.get(string) != null) {
            return data.get(string).getAsFloat();
        }

        return default_;
    }

    public static double getDouble(JsonObject data, String string, double default_) {
        if (data.get(string) != null) {
            return data.get(string).getAsDouble();
        }

        return default_;
    }

    public static String getString(JsonObject data, String string, String default_) {
        if (data.get(string) != null) {
            return data.get(string).getAsString();
        }

        return default_;
    }

    public static ItemStack getItemStack(JsonObject data, String string, ItemStack default_) {
        if (data.get(string) != null) {
            String name = data.get(string).getAsString();
            ItemStack stack = StackHelper.getStackFromString(name);
            return stack == null ? default_ : stack;
        }

        return default_;
    }

    public static Item getItem(JsonObject data, String string, Item default_) {
        if (data.get(string + ":path") != null) {
            ResourceLocation deflt = Item.REGISTRY.getNameForObject(default_);
            String domain = getString(data, string + ":domain", deflt.getResourceDomain());
            String path = getString(data, string + ":path", deflt.getResourcePath());
            Item item = Item.REGISTRY.getObject(new ResourceLocation(domain, path));
            return item == null ? default_ : item;
        }

        return default_;
    }

    public static Block getBlock(JsonObject data, String string, Block default_) {
        if (data.get(string + ":path") != null) {
            ResourceLocation deflt = Block.REGISTRY.getNameForObject(default_);
            String domain = getString(data, string + ":domain", deflt.getResourceDomain());
            String path = getString(data, string + ":path", deflt.getResourcePath());
            Block block = Block.REGISTRY.getObject(new ResourceLocation(domain, path));
            return block == null ? default_ : block;
        }

        return default_;
    }

    public static NBTTagCompound getNBT(JsonObject data, String string, NBTTagCompound default_) {
        if (data.get(string) != null) {
            String name = data.get(string).getAsString();
            NBTTagCompound tag = StackHelper.getTag(new String[] { string }, 0);
            return tag == null ? default_ : tag;
        }

        return default_;
    }

    public static void setEnum(JsonObject data, String string, Enum value, Enum default_) {
        if (value != null && !value.equals(default_)) {
            data.addProperty("enum:" + string, value.name());
        }
    }

    public static void setBoolean(JsonObject data, String string, boolean value, boolean not) {
        if (value != not) {
            data.addProperty(string, value);
        }
    }

    public static void setInteger(JsonObject data, String string, int value, int not) {
        if (value != not) {
            data.addProperty(string, value);
        }
    }

    public static void setFloat(JsonObject data, String string, float value, float not) {
        if (value != not) {
            data.addProperty(string, value);
        }
    }

    public static void setDouble(JsonObject data, String string, double value, double not) {
        if (value != not) {
            data.addProperty(string, value);
        }
    }

    public static void setString(JsonObject data, String string, String value, String not) {
        if (!value.equals(not)) {
            data.addProperty(string, value);
        }
    }

    public static void setItemStack(JsonObject data, String string, ItemStack value, ItemStack dflt) {
        if (value != null && !(value.getItem() == dflt.getItem() && value.getItemDamage() == dflt.getItemDamage())) {
            String name = StackHelper.getStringFromStack(value);
            data.addProperty(string, name);
        }
    }

    public static void setItem(JsonObject data, String string, Item item, Item dflt) {
        if (item != null && item != dflt) {
            ResourceLocation location = Item.REGISTRY.getNameForObject(item);
            ResourceLocation deflt = Item.REGISTRY.getNameForObject(dflt);
            setString(data, string + ":domain", location.getResourceDomain(), deflt.getResourceDomain());
            setString(data, string + ":path", location.getResourcePath(), deflt.getResourcePath());
        }
    }

    public static void setBlock(JsonObject data, String string, Block block, Block dflt) {
        if (block != null && block != dflt) {
            ResourceLocation location = Block.REGISTRY.getNameForObject(block);
            ResourceLocation deflt = Block.REGISTRY.getNameForObject(dflt);

            setString(data, string + ":domain", location.getResourceDomain(), deflt.getResourceDomain());
            setString(data, string + ":path", location.getResourcePath(), deflt.getResourcePath());
        }
    }

    public static void setNBT(JsonObject data, String string, NBTTagCompound value, NBTTagCompound dflt) {
        if (value != null && !value.equals(dflt)) {
            data.addProperty(string, value.toString());
        }
    }

    public static List<IFilterProvider> getItemFilters(JsonObject data, String name, IRule master, boolean isClientside) {
        ArrayList<IFilterProvider> filters = new ArrayList();
        if (data.get(name) == null) return filters;
        JsonArray array = data.get(name).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.get(i).getAsJsonObject();
            String typeName = object.get("type").getAsString();
            JsonObject typeData = object.get("data").getAsJsonObject();
            IFilterProvider filter = RuleHandler.newFilter(master.getProvider(), typeName, typeData, isClientside);
            if (filter != null) {
                filters.add(filter);
            }
        }

        return filters;
    }

    public static void setItemFilters(JsonObject data, String name, List<IFilterProvider> filters) {
        JsonArray array = new JsonArray();
        for (IFilterProvider provider : filters) {
            if (provider == null) continue;
            JsonObject object = new JsonObject();
            object.addProperty("type", provider.getUnlocalisedName());
            JsonObject typeData = new JsonObject();
            writeJSON(typeData, provider.getProvided());
            object.add("data", typeData);
            array.add(object);
        }

        data.add(name, array);
    }

    private static void readEnum(JsonObject json, Field field, Object object, Enum dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getEnum(json, field.getName(), dflt));
    }

    private static void readBoolean(JsonObject json, Field field, IRule object, boolean dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getBoolean(json, field.getName(), dflt));
    }

    private static void readString(JsonObject json, Field field, IRule object, String dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getString(json, field.getName(), dflt));
    }

    private static void readInteger(JsonObject json, Field field, IRule object, int dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getInteger(json, field.getName(), dflt));
    }

    private static void readFloat(JsonObject json, Field field, IRule object, float dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getFloat(json, field.getName(), dflt));
    }

    private static void readDouble(JsonObject json, Field field, IRule object, double dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getDouble(json, field.getName(), dflt));
    }

    private static void readItemFilters(JsonObject json, Field field, IRule object, boolean isClientside) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getItemFilters(json, field.getName(), object, isClientside));
    }

    private static void readItemStack(JsonObject json, Field field, IRule object, ItemStack dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getItemStack(json, field.getName(), dflt));
    }

    private static void readItem(JsonObject json, Field field, IRule object, Item dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getItem(json, field.getName(), dflt));
    }

    private static void readBlock(JsonObject json, Field field, IRule object, Block dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getBlock(json, field.getName(), dflt));
    }

    private static void readNBT(JsonObject json, Field field, IRule object, NBTTagCompound dflt) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, getNBT(json, field.getName(), dflt));
    }

    public static void readVariables(JsonObject json, IRule provider, boolean isClientside) {
        try {
            for (Field field : provider.getClass().getFields()) {
                Object defaultValue = field.get(provider);
                if (provider instanceof IEnum && ((IEnum)provider).isEnum(field.getName())) readEnum(json, field, provider, (Enum) defaultValue);
                if (field.getType() == boolean.class) readBoolean(json, field, provider, (Boolean) defaultValue);
                if (field.getType() == String.class) readString(json, field, provider, (String) defaultValue);
                if (field.getType() == int.class) readInteger(json, field, provider, (Integer) defaultValue);
                if (field.getType() == float.class) readFloat(json, field, provider, (Float) defaultValue);
                if (field.getType() == double.class) readDouble(json, field, provider, (Double) defaultValue);
                if (field.getType() == ItemStack.class) readItemStack(json, field, provider, (ItemStack) defaultValue);
                if (field.getType() == Block.class) readBlock(json, field, provider, (Block) defaultValue);
                if (field.getType() == Item.class) readItem(json, field, provider, (Item) defaultValue);
                if (field.getType() == NBTTagCompound.class) readNBT(json, field, provider, (NBTTagCompound) defaultValue);
                if (field.getGenericType().toString().equals("java.util.List<" + PInfo.FILTER + ">")) readItemFilters(json, field, provider, isClientside);
            }
        } catch (Exception e) {}
    }

    private static void writeEnum(JsonObject json, Field field, IRule object, Enum dflt) throws IllegalArgumentException, IllegalAccessException {
        setEnum(json, field.getName(), (Enum) field.get(object), dflt);
    }

    private static void writeBoolean(JsonObject json, Field field, IRule object, boolean dflt) throws IllegalArgumentException, IllegalAccessException {
        setBoolean(json, field.getName(), (Boolean) field.get(object), dflt);
    }

    private static void writeString(JsonObject json, Field field, IRule object, String dflt) throws IllegalArgumentException, IllegalAccessException {
        setString(json, field.getName(), (String) field.get(object), dflt);
    }

    private static void writeInteger(JsonObject json, Field field, IRule object, int dflt) throws IllegalArgumentException, IllegalAccessException {
        setInteger(json, field.getName(), (Integer) field.get(object), dflt);
    }

    private static void writeFloat(JsonObject json, Field field, IRule object, float dflt) throws IllegalArgumentException, IllegalAccessException {
        setFloat(json, field.getName(), (Float) field.get(object), dflt);
    }

    private static void writeDouble(JsonObject json, Field field, IRule object, double dflt) throws IllegalArgumentException, IllegalAccessException {
        setDouble(json, field.getName(), (Double) field.get(object), dflt);
    }

    private static void writeItemFilters(JsonObject json, Field field, IRule object) throws IllegalArgumentException, IllegalAccessException {
        setItemFilters(json, field.getName(), (List<IFilterProvider>) field.get(object));
    }

    private static void writeItemStack(JsonObject json, Field field, IRule object, ItemStack dflt) throws IllegalArgumentException, IllegalAccessException {
        setItemStack(json, field.getName(), (ItemStack) field.get(object), dflt);
    }

    private static void writeItem(JsonObject json, Field field, IRule object, Item dflt) throws IllegalArgumentException, IllegalAccessException {
        setItem(json, field.getName(), (Item) field.get(object), dflt);
    }

    private static void writeBlock(JsonObject json, Field field, IRule object, Block dflt) throws IllegalArgumentException, IllegalAccessException {
        setBlock(json, field.getName(), (Block) field.get(object), dflt);
    }

    private static void writeNBT(JsonObject json, Field field, IRule object, NBTTagCompound dflt) throws IllegalArgumentException, IllegalAccessException {
        setNBT(json, field.getName(), (NBTTagCompound) field.get(object), dflt);
    }

    public static void writeVariables(JsonObject json, IRule object) {
        try {
            for (Field field : object.getClass().getFields()) {
                Object defaultValue = field.get(getDefault(object).getProvided());
                if (object instanceof IEnum && ((IEnum)object).isEnum(field.getName())) writeEnum(json, field, object, (Enum) defaultValue);
                if (field.getType() == boolean.class) writeBoolean(json, field, object, (Boolean) defaultValue);
                if (field.getType() == String.class) writeString(json, field, object, (String) defaultValue);
                if (field.getType() == int.class) writeInteger(json, field, object, (Integer) defaultValue);
                if (field.getType() == float.class) writeFloat(json, field, object, (Float) defaultValue);
                if (field.getType() == double.class) writeDouble(json, field, object, (Double) defaultValue);
                if (field.getType() == ItemStack.class) writeItemStack(json, field, object, (ItemStack) defaultValue);
                if (field.getType() == Block.class) writeBlock(json, field, object, (Block) defaultValue);
                if (field.getType() == Item.class) writeItem(json, field, object, (Item) defaultValue);
                if (field.getType() == NBTTagCompound.class) writeNBT(json, field, object, (NBTTagCompound) defaultValue);
                if (field.getGenericType().toString().equals("java.util.List<" + PInfo.FILTER + ">")) writeItemFilters(json, field, object);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void readJSON(JsonObject data, IRule provider, boolean isClientside) {
        boolean specialOnly = false;
        if (provider instanceof ISpecialJSON) {
            ISpecialJSON special = ((ISpecialJSON) provider);
            special.readFromJSON(data);
            specialOnly = special.onlySpecial();
        }

        if (!specialOnly) JSONHelper.readVariables(data, provider, isClientside);
    }

    public static void writeJSON(JsonObject data, IRule provider) {
        boolean specialOnly = false;
        if (provider instanceof ISpecialJSON) {
            ISpecialJSON special = ((ISpecialJSON) provider);
            special.writeToJSON(data);
            specialOnly = special.onlySpecial();
        }

        if (!specialOnly) JSONHelper.writeVariables(data, provider);
    }

    private static IRuleProvider getDefault(IRule provider) {
        if (provider instanceof ITrigger) return APIHandler.triggerTypes.get(provider.getProvider().getUnlocalisedName());
        if (provider instanceof IReward) return APIHandler.rewardTypes.get(provider.getProvider().getUnlocalisedName());
        if (provider instanceof ICondition) return APIHandler.conditionTypes.get(provider.getProvider().getUnlocalisedName());
        if (provider instanceof IFilter) return APIHandler.filterTypes.get(provider.getProvider().getUnlocalisedName());

        //WHAT
        return null;
    }
}
