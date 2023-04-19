package de.thedead2.progression_reloaded.network;

import io.netty.buffer.ByteBuf;
import de.thedead2.progression_reloaded.helpers.ChatHelper;
import de.thedead2.progression_reloaded.network.core.PenguinPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

@Packet(isSided = true, side = Side.CLIENT)
public class PacketDisplayChat extends PenguinPacket {
    private String text;

    public PacketDisplayChat() {}
    public PacketDisplayChat(String text) {
        this.text = text;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeGzipString(buf, text);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        text = readGzipString(buf);
    }

    @Override
    public void handlePacket(EntityPlayer sender) {
        ChatHelper.displayChat(text);
    }
}
