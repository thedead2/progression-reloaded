package de.thedead2.progression_reloaded.player.nbt;

import de.thedead2.progression_reloaded.helpers.NBTHelper.IMapHelper;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class CustomNBT implements IMapHelper {
    public static final CustomNBT INSTANCE = new CustomNBT();
    
    public Map map;
    
    public IMapHelper setMap(Map map) {
        this.map = map;
        return this;
    }

    @Override
    public Map getMap() {
        return map;
    }

    @Override
    public Object readKey(NBTTagCompound tag) {
        return (String)tag.getString("Name");
    }

    @Override
    public Object readValue(NBTTagCompound tag) {
        return (NBTTagCompound)tag.getTag("Tag");
    }

    @Override
    public void writeKey(NBTTagCompound tag, Object o) {
        tag.setString("Name", (String)o);
    }

    @Override
    public void writeValue(NBTTagCompound tag, Object o) {
        tag.setTag("Tag", (NBTTagCompound)o);
    }
}
