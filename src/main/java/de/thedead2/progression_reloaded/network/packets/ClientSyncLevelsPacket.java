package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class ClientSyncLevelsPacket implements ModNetworkPacket {

    private final ResourceLocation currentLevel;

    private final Map<ResourceLocation, LevelProgress> progress;


    public ClientSyncLevelsPacket(ProgressionLevel currentLevel, Map<ProgressionLevel, LevelProgress> levelProgress) {
        this.currentLevel = currentLevel.getId();
        this.progress = CollectionHelper.convertMapKeys(levelProgress, ProgressionLevel::getId);
    }


    @SuppressWarnings("unused")
    public ClientSyncLevelsPacket(FriendlyByteBuf buf) {
        this.currentLevel = buf.readResourceLocation();
        this.progress = buf.readMap(FriendlyByteBuf::readResourceLocation, LevelProgress::fromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                var client = ModClientInstance.getInstance();
                var clientDataManager = client.getClientDataManager();
                clientDataManager.updateLevelProgress(ClientSyncLevelsPacket.this.currentLevel, ClientSyncLevelsPacket.this.progress);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.currentLevel);
        buf.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (buf1, levelProgress) -> levelProgress.toNetwork(buf1));
    }
}
