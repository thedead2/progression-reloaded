package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketSyncTriggers extends PenguinPacket {
    private Set<ITriggerProvider> triggers;
    private boolean overwrite;

    public PacketSyncTriggers() {}
    public PacketSyncTriggers(Set<ITriggerProvider> triggers) {
        this.overwrite = true;
        this.triggers = triggers;
    }

    public PacketSyncTriggers(ITriggerProvider trigger) {
        this.overwrite = false;
        this.triggers = new HashSet();
        this.triggers.add(trigger);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(overwrite);
        buf.writeInt(triggers.size());
        for (ITriggerProvider trigger: triggers) {
            writeGzipString(buf, trigger.getUniqueID().toString());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        overwrite = buf.readBoolean();
        int size = buf.readInt();
        triggers = new HashSet();
        for (int i = 0; i < size; i++) {
            String uuid = readGzipString(buf);
            if (uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                ITriggerProvider trigger = APICache.getClientCache().getTriggerFromUUID(UUID.fromString(uuid));
                if (trigger != null) {
                    triggers.add(trigger);
                }
            }
        }
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTracker.getClientPlayer().getMappings().markTriggerAsCompleted(overwrite, triggers);
    }
}
