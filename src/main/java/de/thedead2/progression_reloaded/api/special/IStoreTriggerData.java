package de.thedead2.progression_reloaded.api.special;

import net.minecraft.nbt.NBTTagCompound;

/** Implement this on triggers that need to store data **/
public interface IStoreTriggerData {
    public void readDataFromNBT(NBTTagCompound tag);

    public void writeDataToNBT(NBTTagCompound tag);
}
