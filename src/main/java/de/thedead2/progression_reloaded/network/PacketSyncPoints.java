package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.network.core.PacketNBT;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import de.thedead2.progression_reloaded.player.data.Points;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketSyncPoints extends PacketNBT {
    public PacketSyncPoints() {}
    public PacketSyncPoints(INBTWritable readable) {
        super(readable);
    }
    
    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTracker.getClientPlayer().setPoints(new Points().readFromNBT(tag));
    }
}
