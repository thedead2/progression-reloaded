package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.lib.GuiIDs;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketOpenEditor extends PenguinPacket {
    @Override
    public void handlePacket(EntityPlayer player) {
        MCClientHelper.getPlayer().openGui(de.thedead2.progression_reloaded.ProgressionReloaded.instance, GuiIDs.EDITOR, null, 0, 0, 0);
    }
}
