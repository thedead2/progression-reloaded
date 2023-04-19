package de.thedead2.progression_reloaded.player.nbt;

import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.handlers.APICache;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.UUID;

public class RewardSet extends AbstractUniqueNBT {
    public static final RewardSet INSTANCE = new RewardSet();

    @Override
    public NBTBase write(Object s) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Reward", ((IRewardProvider) s).getUniqueID().toString());
        return tag;
    }

    @Override
    public Object read(NBTTagList list, int i) {
        NBTTagCompound tag = list.getCompoundTagAt(i);
        return APICache.getCache(false).getRewardFromUUID(UUID.fromString(tag.getString("Reward")));
    }
}
