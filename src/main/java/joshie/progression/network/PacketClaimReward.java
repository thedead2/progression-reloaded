package joshie.progression.network;

import io.netty.buffer.ByteBuf;
import joshie.progression.api.criteria.IProgressionCriteria;
import joshie.progression.api.criteria.IProgressionReward;
import joshie.progression.handlers.APIHandler;
import joshie.progression.network.core.PenguinPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PacketClaimReward extends PenguinPacket {
    private IProgressionCriteria criteria;
    private int rewardId;
    private boolean randomReward;

    public PacketClaimReward() {}

    public PacketClaimReward(IProgressionCriteria criteria, int rewardId, boolean randomReward) {
        this.criteria = criteria;
        this.rewardId = rewardId;
        this.randomReward = randomReward;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, criteria.getUniqueID().toString());
        buf.writeInt(rewardId);
        buf.writeBoolean(randomReward);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        criteria = APIHandler.getCriteriaFromName(UUID.fromString(ByteBufUtils.readUTF8String(buf)));
        rewardId = buf.readInt();
        randomReward = buf.readBoolean();
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        List<IProgressionReward> rewards = criteria.getRewards();
        if (rewards != null && rewards.size() > 0) {
            if (randomReward) Collections.shuffle(rewards);
            int selected = 0;
            //Looping around to avoid getting wrong values
            for (int i = 0; i < rewards.size(); i++) {
                if (i == rewardId) {
                    selected = i;
                    break;
                }
            }

            rewards.get(selected).reward((EntityPlayerMP)player);
        }
    }
}
