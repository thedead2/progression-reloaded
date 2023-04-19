package de.thedead2.progression_reloaded.lib;

import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class WorldLocation {
    public EntityPlayer player;
    public int dimension;
    public BlockPos pos;

    public WorldLocation(int dimension, double x, double y, double z) {
        this.dimension = dimension;
        this.pos = new BlockPos(x, y, z);
    }

    public WorldLocation(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = new BlockPos(pos);
    }

    public WorldLocation(EntityPlayer player) {
        this.dimension = player.dimension;
        this.pos = new BlockPos(player);
        this.player = player;
    }
    
    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    public static WorldLocation getRandomLocationFromFilters(List<IFilterProvider> locality, EntityPlayer player) {
        int size = locality.size();
        if (size == 0) return null;
        if (size == 1) return (WorldLocation) locality.get(0).getProvided().getRandom(player);
        else {
            return (WorldLocation) locality.get(player.worldObj.rand.nextInt(size)).getProvided().getRandom(player);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorldLocation location = (WorldLocation) o;
        if (dimension != location.dimension) return false;
        return pos != null ? pos.equals(location.pos) : location.pos == null;

    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        return result;
    }
}
