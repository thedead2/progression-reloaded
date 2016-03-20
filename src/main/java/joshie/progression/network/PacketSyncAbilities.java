package joshie.progression.network;

import joshie.progression.network.core.PacketNBT;
import joshie.progression.player.PlayerTracker;
import joshie.progression.player.data.AbilityStats;
import net.minecraft.entity.player.EntityPlayer;

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
