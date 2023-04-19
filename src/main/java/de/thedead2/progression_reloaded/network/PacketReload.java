package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.handlers.RemappingHandler;
import de.thedead2.progression_reloaded.helpers.ChatHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.json.DefaultSettings;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.json.Options;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;

@Packet
public class PacketReload extends PenguinPacket {
    @Override
    public void handlePacket(EntityPlayer player) {
        String hostname = player.worldObj.isRemote ? JSONLoader.serverName : RemappingHandler.getHostName();
        PacketReload.handle(JSONLoader.getServerTabData(hostname), player.worldObj.isRemote);
    }

    public static void handle(DefaultSettings settings, boolean isClient) {
        if (isClient) {
            ChatHelper.displayChat("Progression data was reloaded", "   Use " + TextFormatting.BLUE + "/progression reset" + TextFormatting.RESET + " if you wish to reset player data");
        } else {
            if (Options.editor) {
                //Perform a reset of all the data serverside
                RemappingHandler.reloadServerData(settings, false);
                for (EntityPlayer player : PlayerHelper.getAllPlayers()) {
                    RemappingHandler.onPlayerConnect((EntityPlayerMP) player);
                }

                PacketHandler.sendToEveryone(new PacketReload());
            }
        }
    }
}
