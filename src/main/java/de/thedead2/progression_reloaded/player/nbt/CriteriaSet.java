package de.thedead2.progression_reloaded.player.nbt;

import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.handlers.APICache;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.UUID;

public class CriteriaSet extends AbstractUniqueNBT {
    public static final CriteriaSet INSTANCE = new CriteriaSet();

    @Override
    public NBTBase write(Object s) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Criteria", ((ICriteria) s).getUniqueID().toString());
        return tag;
    }

    @Override
    public Object read(NBTTagList list, int i) {
        NBTTagCompound tag = list.getCompoundTagAt(i);
        return APICache.getServerCache().getCriteria(UUID.fromString(tag.getString("Criteria")));
    }
}
