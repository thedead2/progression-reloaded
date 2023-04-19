package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.helpers.ChatHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.UUID;

import static de.thedead2.progression_reloaded.gui.core.GuiList.GROUP_EDITOR;
import static net.minecraft.util.text.TextFormatting.*;

@Packet
public class PacketInvitePlayer extends PenguinPacket {
    private UUID teamOwner;
    private String teamName;
    private String username;

    public PacketInvitePlayer() {}
    public PacketInvitePlayer(UUID teamOwner, String team, String username) {
        this.teamOwner = teamOwner;
        this.teamName = team;
        this.username = username;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeGzipString(buf, teamOwner.toString());
        writeGzipString(buf, teamName);
        writeGzipString(buf, username);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        teamOwner = UUID.fromString(readGzipString(buf));
        teamName = readGzipString(buf);
        username = readGzipString(buf);
    }

    public static void sendInvite(EntityPlayer sender, String username, UUID teamOwner, String teamName) {
        if (sender.getName().equalsIgnoreCase(username)) {
            PacketHandler.sendToClient(new PacketDisplayChat(RED + "You cannot invite yourself!"), sender);
        } else {
            PlayerTeam team = PlayerTracker.getPlayerData(sender).getTeam();
            if (team.isSingle()) {
                PacketHandler.sendToClient(new PacketDisplayChat(RED + "You cannot invite people to a single player team!"), sender);
            } else if (!team.isOwner(sender)) {
                PacketHandler.sendToClient(new PacketDisplayChat(RED + "Only the owner can invite players!"), sender);
            } else {
                for (EntityPlayerMP player : PlayerHelper.getAllPlayers()) {
                    if (player.getName().equalsIgnoreCase(username)) {
                        PacketHandler.sendToClient(new PacketInvitePlayer(teamOwner, teamName, username), player);
                        PacketHandler.sendToClient(new PacketDisplayChat(GREEN + "You successfully invited " + username + " to your team!"), sender);
                        team.addToInvited(PlayerHelper.getUUIDForPlayer(player));
                        return;
                    }
                }

                PacketHandler.sendToClient(new PacketDisplayChat(RED + username + " does not exist or is not online, invite failed!"), sender);
            }
        }
    }

    @Override
    public void handlePacket(EntityPlayer sender) {
        if (!sender.worldObj.isRemote) {
            sendInvite(sender, username, teamOwner, teamName);
        } else {
            GROUP_EDITOR.addInvite(teamOwner, teamName);
            ChatHelper.displayChat(BLUE + "You were invited to join the team: " + WHITE +  teamName + "\n" + RED + "If you wish to join this team, use the progression book or type" + GREEN + "\n/progression team join " + teamName);
        }
    }
}
