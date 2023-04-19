package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.PClientProxy;
import de.thedead2.progression_reloaded.helpers.SplitHelper;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import net.minecraft.entity.player.EntityPlayer;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CORE;
import static de.thedead2.progression_reloaded.network.core.PacketPart.SEND_SIZE;

@Packet
public class PacketLockUnlockSaving extends PenguinPacket {
    private boolean lock;

    public PacketLockUnlockSaving() {}
    public PacketLockUnlockSaving(boolean lock) {
        this.lock = lock;
    }

    @Override
    public void toBytes(ByteBuf to) {
        to.writeBoolean(lock);
    }

    @Override
    public void fromBytes(ByteBuf from) {
        lock = from.readBoolean();
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        if (!player.worldObj.isRemote) PacketHandler.sendToEveryone(new PacketLockUnlockSaving(true)); //If we're server, tell everyone they CANNOT EDIT
        else { //If we're client lets check some stuff
            PClientProxy.bookLocked = lock; //Lock the book
            if (PClientProxy.isSaver && lock) { //If we were the person saving
                //Now save everything :)
                JSONLoader.saveData(true); //Save the data clientside
                String json = JSONLoader.getClientTabJsonData();
                int length = SplitHelper.splitStringEvery(json, JSONLoader.MAX_LENGTH).length;
                //Send the packet to the server about the new json
                PacketHandler.sendToServer(new PacketSyncJSONToServer(SEND_SIZE, "", length, System.currentTimeMillis()));
                CORE.clearEditors();
            }
        }
    }
}
