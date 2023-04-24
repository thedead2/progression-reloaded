package de.thedead2.progression_reloaded.player;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.player.PlayerSavedData.TeamAction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PlayerTracker {
    private static final HashMap<TileEntity, UUID> owners = Maps.newHashMap();
    private static final HashSet<TileEntity> loaded = new HashSet();

    @OnlyIn(Dist.CLIENT)
    public static PlayerDataClient getClientPlayer() {
        return PlayerDataClient.getInstance();
    }

    public static PlayerDataServer getServerPlayer(EntityPlayer player) {
        return getServerPlayer(PlayerHelper.getUUIDForPlayer(player));
    }

    public static boolean reset(String username) {
        return de.thedead2.progression_reloaded.ProgressionReloaded.data.reset(username);
    }

    public static PlayerDataServer getServerPlayer(UUID uuid) {
        return de.thedead2.progression_reloaded.ProgressionReloaded.data.getServerPlayer(uuid);
    }

    public static void joinTeam(EntityPlayer player, TeamAction action, UUID team, String name) {
        de.thedead2.progression_reloaded.ProgressionReloaded.data.joinTeam(player, action, team, name);
    }

    public static PlayerDataCommon getPlayerData(UUID uuid, boolean isClient) {
        return isClient ? getClientPlayer() : getServerPlayer(uuid);
    }

    public static PlayerDataCommon getPlayerData(EntityPlayer player) {
        return getPlayerData(PlayerHelper.getUUIDForPlayer(player), player.worldObj.isRemote);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlaced(PlaceEvent event) {
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (event.getPlayer() != null && tile != null) {
            setTileOwner(tile, PlayerHelper.getUUIDForPlayer(event.getPlayer()));
        }
    }

    public static UUID getTileOwner(TileEntity tile) {
        UUID uuid = owners.get(tile);
        if (uuid == null && !loaded.contains(tile)) {
            //Attempt to load it from the tile
            if (tile.getTileData().hasKey("Progression")) {
                String id = tile.getTileData().getCompoundTag("Progression").getString("Owner");
                uuid = UUID.fromString(id);
                if (uuid != null) {
                    owners.put(tile, uuid);
                }
            }
            
            loaded.add(tile);
        }

        return uuid;
    }

    public static void setTileOwner(TileEntity tile, UUID owner) {
        owners.put(tile, owner);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Owner", owner.toString());
        tile.getTileData().setTag("Progression", tag);
        tile.markDirty();
    }
}
