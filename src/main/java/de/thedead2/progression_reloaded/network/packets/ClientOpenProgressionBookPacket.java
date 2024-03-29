package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.screens.QuestLogScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientOpenProgressionBookPacket implements ModNetworkPacket {

    public ClientOpenProgressionBookPacket() {}


    @SuppressWarnings("unused")
    public ClientOpenProgressionBookPacket(FriendlyByteBuf buf) {}


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new QuestLogScreen(ModClientInstance.getInstance().getClientData().getPlayerQuests()));
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {}
}
