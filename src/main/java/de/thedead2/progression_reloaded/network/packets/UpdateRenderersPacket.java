package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class UpdateRenderersPacket implements ModNetworkPacket {

    public UpdateRenderersPacket() {}


    @SuppressWarnings("unused")
    public UpdateRenderersPacket(FriendlyByteBuf buf) {}


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().levelRenderer.allChanged();
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }
}
