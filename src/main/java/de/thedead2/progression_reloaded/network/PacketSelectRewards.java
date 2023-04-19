package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.CriteriaMappings;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Packet(isSided = true, side = Side.SERVER)
public class PacketSelectRewards extends PenguinPacket {
    private Set<IRewardProvider> rewards;

    public PacketSelectRewards() {}

    public PacketSelectRewards(Set<IRewardProvider> rewards) {
        this.rewards = rewards;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(rewards.size());
        for (IRewardProvider provider: rewards) {
            writeGzipString(buf, provider.getUniqueID().toString());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rewards = new LinkedHashSet();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            rewards.add(APICache.getCache(false).getRewardFromUUID(UUID.fromString(readGzipString(buf))));
        }
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        for (IRewardProvider reward: rewards) {
            CriteriaMappings mappings = PlayerTracker.getServerPlayer(player).getMappings();
            if (mappings.claimReward((EntityPlayerMP) player, reward)) {
                mappings.remapAfterClaiming(reward.getCriteria());
            }
        }
    }
}
