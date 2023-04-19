package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketSyncCriteria extends PenguinPacket {
    private ICriteria[] criteria;
    private Integer[] integers;
    private boolean overwrite;

    public PacketSyncCriteria() {}

    public PacketSyncCriteria(boolean overwrite, Integer[] values, ICriteria[] criteria) {
        this.criteria = criteria;
        this.integers = values;
        this.overwrite = overwrite;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(overwrite);
        buf.writeInt(criteria.length);
        for (ICriteria tech : criteria) {
            writeGzipString(buf, tech.getUniqueID().toString());
        }

        for (Integer i : integers) {
            buf.writeInt(i);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        overwrite = buf.readBoolean();
        int size = buf.readInt();
        criteria = new ICriteria[size];
        for (int i = 0; i < size; i++) {
            criteria[i] = APICache.getClientCache().getCriteria(UUID.fromString(readGzipString(buf)));
        }

        integers = new Integer[size];
        for (int i = 0; i < size; i++) {
            integers[i] = buf.readInt();
        }
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTracker.getClientPlayer().getMappings().markCriteriaAsCompleted(overwrite, integers, criteria);
    }
}
