package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerSavedData.TeamAction;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

@Packet(isSided = true, side = Side.SERVER)
public class PacketChangeTeam extends PenguinPacket {
    private TeamAction action;
    private String name;
    private UUID team;

    public PacketChangeTeam() {}
    public PacketChangeTeam(TeamAction action) {
        this.action = action;
    }

    public PacketChangeTeam(TeamAction action, String name) {
        this.action = action;
        this.name = name;
    }

    public PacketChangeTeam(TeamAction action, UUID team) {
        this(action);
        this.team = team;
    }

    @Override
    public void toBytes(ByteBuf to) {
        if (name != null) {
            to.writeBoolean(true);
            writeGzipString(to, name);
        } else to.writeBoolean(false);

        to.writeInt(action.ordinal());
        if (team != null) {
            to.writeBoolean(true);
            ByteBufUtils.writeUTF8String(to, team.toString());
        } else to.writeBoolean(false);
    }

    @Override
    public void fromBytes(ByteBuf from) {
        if (from.readBoolean()) {
            name = readGzipString(from);
        }

        action = TeamAction.values()[from.readInt()];
        if (from.readBoolean()) {
            team = UUID.fromString(ByteBufUtils.readUTF8String(from));
        }
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        PlayerTracker.joinTeam(player, action, team, name);
    }
}
