package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ClientDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class ClientSyncPlayerPacket implements ModNetworkPacket {

    @Nullable
    private final PlayerData playerData;


    public ClientSyncPlayerPacket(@Nullable PlayerData player) {
        this.playerData = player;
    }


    @SuppressWarnings("unused")
    public ClientSyncPlayerPacket(FriendlyByteBuf buf) {
        this.playerData = buf.readNullable(PlayerData::fromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientDataManager.getInstance().setClientData(ClientSyncPlayerPacket.this.playerData);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNullable(this.playerData, (buf1, playerData1) -> playerData1.serializeToNetwork(buf1));
    }
}
