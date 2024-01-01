package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModLoginNetworkPacket;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientWelcomePacket implements ModLoginNetworkPacket {

    private int index;


    public ClientWelcomePacket() {}


    public ClientWelcomePacket(FriendlyByteBuf buf) {}


    @Override
    public void setLoginIndex(int i) {
        this.index = i;
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ModHelper.LOGGER.warn("Running login message!");
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }


    @Override
    public int getAsInt() {
        return index;
    }
}
