package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.client.gui.ProgressionBookGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientOpenProgressionBookPacket implements ModNetworkPacket {

    public ClientOpenProgressionBookPacket(){
    }
    public ClientOpenProgressionBookPacket(FriendlyByteBuf buf){
    }
    @Override
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        //For whatever reason this cannot be a lambda, it has to be an anonymous inner class?!
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new ProgressionBookGUI());
            }
        };
    }

    @Override
    public DistExecutor.SafeRunnable onServer(Supplier<NetworkEvent.Context> ctx) {
        return null;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {}
}
