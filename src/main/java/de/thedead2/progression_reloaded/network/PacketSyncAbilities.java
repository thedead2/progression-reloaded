package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.network.core.PacketNBT;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import de.thedead2.progression_reloaded.player.data.AbilityStats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketSyncAbilities extends PacketNBT {
    public PacketSyncAbilities() {}
    public PacketSyncAbilities(INBTWritable readable) {
        super(readable);
    }
    
    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTracker.getClientPlayer().setAbilities(new AbilityStats().readFromNBT(tag));
    }
}
