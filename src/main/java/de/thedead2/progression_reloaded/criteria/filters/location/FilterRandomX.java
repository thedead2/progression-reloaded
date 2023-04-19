package de.thedead2.progression_reloaded.criteria.filters.location;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IEnum;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

@ProgressionRule(name="randomx", color=0xFFBBBBBB)
public class FilterRandomX extends FilterRandomCoordinate implements IEnum {
    public FilterRandomX() {
        super("randomx");
    }

    @Override
    public BlockPos getLocation(EntityPlayer player, int random) {
        return new BlockPos(coordinate + random, player.posY, player.posZ);
    }

    @Override
    public int getCoordinate(WorldLocation location) {
        return location.pos.getX();
    }
}
