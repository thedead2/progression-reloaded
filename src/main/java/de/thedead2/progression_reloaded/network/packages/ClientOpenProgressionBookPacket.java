package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.client.gui.ProgressionBookGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientOpenProgressionBookPacket implements ModNetworkPacket{

    public ClientOpenProgressionBookPacket(){
    }
    @SuppressWarnings("unused")
    public ClientOpenProgressionBookPacket(FriendlyByteBuf buf){
    }
    @Override
    @SuppressWarnings("all")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft.getInstance().setScreen(new ProgressionBookGUI(Minecraft.getInstance().player));
            }
        };
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }
}
