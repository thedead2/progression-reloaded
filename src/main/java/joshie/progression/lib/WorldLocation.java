package joshie.progression.lib;

import joshie.progression.api.criteria.IProgressionFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.Random;

public class WorldLocation {
    private static final Random rand = new Random();
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
    
    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    public static WorldLocation getRandomLocationFromFilters(List<IProgressionFilter> locality, EntityPlayer player) {
        int size = locality.size();
        if (size == 0) return null;
        if (size == 1) return (WorldLocation) locality.get(0).getRandom(player);
        else {
            return (WorldLocation) locality.get(rand.nextInt(size));
        }
    }
}
