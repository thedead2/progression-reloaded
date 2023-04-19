package de.thedead2.progression_reloaded.player.data;

import de.thedead2.progression_reloaded.helpers.NBTHelper;
import de.thedead2.progression_reloaded.network.core.PacketNBT.INBTWritable;
import de.thedead2.progression_reloaded.player.nbt.CustomNBT;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public class CustomStats implements INBTWritable<CustomStats> {
    private HashMap<String, NBTTagCompound> customData = new HashMap();

    public NBTTagCompound getCustomData(String key) {
        return customData.get(key);
    }

    public void setCustomData(String key, NBTTagCompound tag) {
        if (key == null || tag == null) return; //Don't add nulls
        customData.put(key, tag);
    }

    @Override
    public CustomStats readFromNBT(NBTTagCompound tag) {
        NBTHelper.readMap(tag, "Custom", CustomNBT.INSTANCE.setMap(customData));
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTHelper.writeMap(tag, "Custom", CustomNBT.INSTANCE.setMap(customData));
        return tag;
    }
}
