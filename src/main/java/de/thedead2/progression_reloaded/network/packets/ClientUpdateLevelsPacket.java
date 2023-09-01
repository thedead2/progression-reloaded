package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.client.ClientManager;
import de.thedead2.progression_reloaded.client.data.ClientLevel;
import de.thedead2.progression_reloaded.client.data.ClientLevelProgress;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ClientUpdateLevelsPacket implements ModNetworkPacket {

    private final ClientLevel currentLevel;

    private final Map<ClientLevel, ClientLevelProgress> progress;


    public ClientUpdateLevelsPacket(KnownPlayer player, ProgressionLevel currentLevel, Map<ProgressionLevel, LevelProgress> levelProgress) {
        this.currentLevel = ClientLevel.fromProgressionLevel(currentLevel);
        this.progress = convertToClientLevelProgress(levelProgress, player);
    }


    private static Map<ClientLevel, ClientLevelProgress> convertToClientLevelProgress(Map<ProgressionLevel, LevelProgress> levelProgress, KnownPlayer player) {
        Map<ClientLevel, ClientLevelProgress> clientProgress = new HashMap<>();
        levelProgress.forEach((level, progress) -> clientProgress.put(ClientLevel.fromProgressionLevel(level), ClientLevelProgress.fromLevelProgress(progress, player)));

        return clientProgress;
    }


    @SuppressWarnings("unused")
    public ClientUpdateLevelsPacket(FriendlyByteBuf buf) {
        this.currentLevel = ClientLevel.fromNetwork(buf);
        this.progress = buf.readMap(ClientLevel::fromNetwork, ClientLevelProgress::fromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientManager.getInstance().getPlayer().getLevels().updateLevels(ClientUpdateLevelsPacket.this.currentLevel, ClientUpdateLevelsPacket.this.progress);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        this.currentLevel.toNetwork(buf);
        buf.writeMap(this.progress, (buf1, clientLevel) -> clientLevel.toNetwork(buf1), (buf1, clientLevelProgress) -> clientLevelProgress.toNetwork(buf1));
    }
}
