package de.thedead2.progression_reloaded.player.nbt;

import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.helpers.NBTHelper.IMapHelper;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.UUID;

public class CriteriaNBT implements IMapHelper {
    public static final CriteriaNBT INSTANCE = new CriteriaNBT();

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
        String name = tag.getString("Name");
        return APICache.getServerCache().getCriteria(UUID.fromString(name));
    }

    @Override
    public Object readValue(NBTTagCompound tag) {
        return (Integer)tag.getInteger("Number");
    }

    @Override
    public void writeKey(NBTTagCompound tag, Object o) {
        String name = ((ICriteria)o).getUniqueID().toString();
        tag.setString("Name", name);
    }

    @Override
    public void writeValue(NBTTagCompound tag, Object o) {
        tag.setInteger("Number", (Integer)o);
    }
}
