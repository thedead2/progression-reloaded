package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.client.ClientManager;
import de.thedead2.progression_reloaded.client.display.TeamDisplayInfo;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class SyncPlayerPacket implements ModNetworkPacket {

    private final int extraLives;

    @Nullable
    private final TeamDisplayInfo team;


    public SyncPlayerPacket(SinglePlayer player) {
        this.extraLives = player.getExtraLives();
        this.team = player.getTeam().isPresent() ? player.getTeam().get().getDisplay() : null;
    }


    @SuppressWarnings("unused")
    public SyncPlayerPacket(FriendlyByteBuf buf) {
        this.extraLives = buf.readInt();
        this.team = buf.readNullable(TeamDisplayInfo::fromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientManager.getInstance().getPlayer().onSync(SyncPlayerPacket.this.extraLives, SyncPlayerPacket.this.team);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.extraLives);
        buf.writeNullable(this.team, (buf1, teamDisplayInfo) -> teamDisplayInfo.toNetwork(buf1));
    }
}
