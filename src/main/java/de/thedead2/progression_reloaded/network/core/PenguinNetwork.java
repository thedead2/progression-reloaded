package de.thedead2.progression_reloaded.network.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PenguinNetwork {
    private final SimpleNetworkWrapper INSTANCE;
    private final PenguinPacketHandler handler;
    private int id;
    
    public PenguinNetwork(String name) {
        INSTANCE = new SimpleNetworkWrapper(name);
        handler = new PenguinPacketHandler();
    }

    public void registerPacket(Class clazz, Side side) {
        INSTANCE.registerMessage(handler, clazz, id++, side);
    }
    
    public void sendToClient(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    public void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }
    
    public void sendToEveryone(IMessage message) {
        INSTANCE.sendToAll(message);
    }
}
