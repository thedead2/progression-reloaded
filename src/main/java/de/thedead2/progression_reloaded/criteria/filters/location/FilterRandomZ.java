package de.thedead2.progression_reloaded.criteria.filters.location;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IEnum;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

@ProgressionRule(name="randomz", color=0xFFBBBBBB)
public class FilterRandomZ extends FilterRandomCoordinate implements IEnum {
    public FilterRandomZ() {
        super("randomz");
    }

    @Override
    public BlockPos getLocation(EntityPlayer player, int random) {
        return new BlockPos(player.posX, player.posY, coordinate + random);
    }

    @Override
    public int getCoordinate(WorldLocation location) {
        return location.pos.getZ();
    }
}
