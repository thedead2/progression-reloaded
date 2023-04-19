package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

@Packet
public class PacketSyncTeam extends PenguinPacket {
    private NBTTagCompound tag;

    public PacketSyncTeam() {}

    public PacketSyncTeam(PlayerTeam team) {
        tag = new NBTTagCompound();
        team.writeToNBT(tag);
    }

    @Override
    public void toBytes(ByteBuf to) {
        ByteBufUtils.writeTag(to, tag);
    }

    @Override
    public void fromBytes(ByteBuf from) {
        tag = ByteBufUtils.readTag(from);
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTeam team = new PlayerTeam();
        team.readFromNBT(tag);
        
        //Server to Client
        if (player.worldObj.isRemote) {
            PlayerTracker.getClientPlayer().setTeam(team);
        } else {
            //Client to server
            PlayerTracker.getServerPlayer(player).setTeam(team);
        }
    }
}
