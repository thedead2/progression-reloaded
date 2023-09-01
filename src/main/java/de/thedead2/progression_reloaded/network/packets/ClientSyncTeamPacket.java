package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.client.ClientManager;
import de.thedead2.progression_reloaded.client.display.TeamDisplayInfo;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class ClientSyncTeamPacket implements ModNetworkPacket {

    private final TeamDisplayInfo team;


    public ClientSyncTeamPacket(@Nullable PlayerTeam team) {
        this.team = team != null ? team.getDisplay() : null;
    }


    @SuppressWarnings("unused")
    public ClientSyncTeamPacket(FriendlyByteBuf buf) {
        this.team = buf.readNullable(TeamDisplayInfo::fromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientManager.getInstance().getPlayer().updateTeam(ClientSyncTeamPacket.this.team);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNullable(this.team, (buf1, teamDisplayInfo) -> teamDisplayInfo.toNetwork(buf1));
    }
}
