package de.thedead2.progression_reloaded.criteria.filters.location;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IHasEventBus;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

@ProgressionRule(name="playerBroken", color=0xFFBBBBBB)
public class FilterPlayerLastBroken extends FilterLocationBase implements IHasEventBus {
    private static final HashMap<UUID, BlockPos> cache = new HashMap();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null) {
            cache.put(PlayerHelper.getUUIDForPlayer(event.getPlayer()), event.getPos());
        }
    }

    @Override
    public EventBus getEventBus() {
        return MinecraftForge.EVENT_BUS;
    }

    @Override
    public WorldLocation getRandom(EntityPlayer player) {
        UUID uuid = PlayerHelper.getUUIDForPlayer(player);
        if (cache.get(uuid) == null) return null;
        else return new WorldLocation(player.dimension, cache.get(uuid));
    }

    @Override
    public boolean matches(WorldLocation location) {
        return true;
    }
}
