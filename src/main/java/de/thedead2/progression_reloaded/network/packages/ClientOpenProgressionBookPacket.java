package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.client.gui.ProgressionBookGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientOpenProgressionBookPacket extends ModNetworkPacket {
    public ClientOpenProgressionBookPacket(){}
    public ClientOpenProgressionBookPacket(FriendlyByteBuf buf){}
    @Override
    public void onClient(Supplier<NetworkEvent.Context> ctx) {
        Minecraft.getInstance().setScreen(new ProgressionBookGUI());
    }
}
