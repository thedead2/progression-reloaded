package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.handlers.RemappingHandler;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.json.Options;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;

@Packet
public class PacketReset extends PenguinPacket {
    private boolean singlePlayer;
    private String username;

    public PacketReset() {
        singlePlayer = false;
    }

    public PacketReset(String string) {
        singlePlayer = true;
        username = string;
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        PacketReset.handle(player, singlePlayer, username);
    }

    @Override
    public void toBytes(ByteBuf to) {
        to.writeBoolean(singlePlayer);
        if (singlePlayer) {
            ByteBufUtils.writeUTF8String(to, username);
        }
    }

    @Override
    public void fromBytes(ByteBuf from) {
        singlePlayer = from.readBoolean();
        if (singlePlayer) {
            username = ByteBufUtils.readUTF8String(from);
        }
    }

    public static void handle(EntityPlayer sender, boolean singlePlayer, String username) {
        if (sender.worldObj.isRemote) {
            if (!singlePlayer) MCClientHelper.getPlayer().addChatComponentMessage(new TextComponentString("All player data for Progression was reset."));
            else MCClientHelper.getPlayer().addChatComponentMessage(new TextComponentString("All player data for " + username + " was reset."));
        } else {
            if (Options.editor) {
                if (!singlePlayer) {
                    if (Options.hardReset) de.thedead2.progression_reloaded.ProgressionReloaded.instance.createWorldData(); //Recreate the world data, Wiping out any saved information for players
                    else de.thedead2.progression_reloaded.ProgressionReloaded.data.clear();
                    RemappingHandler.reloadServerData(JSONLoader.getServerTabData(RemappingHandler.getHostName()), false);
                    for (EntityPlayerMP player : PlayerHelper.getAllPlayers()) {
                        //Reset all the data to default
                        RemappingHandler.onPlayerConnect(player);
                    }

                    PacketHandler.sendToEveryone(new PacketReset());
                } else {
                    if (PlayerTracker.reset(username) && sender instanceof EntityPlayerMP) {
                        PacketHandler.sendToClient(new PacketReset(username), sender);
                    }
                }
            }
        }
    }
}
