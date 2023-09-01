package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.client.ClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientSyncExtraLives implements ModNetworkPacket {

    private final int extraLives;


    public ClientSyncExtraLives(int extraLives) {this.extraLives = extraLives;}


    @SuppressWarnings("unused")
    public ClientSyncExtraLives(FriendlyByteBuf buf) {this.extraLives = buf.readInt();}


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientManager.getInstance().getPlayer().updateExtraLives(ClientSyncExtraLives.this.extraLives);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.extraLives);
    }
}
