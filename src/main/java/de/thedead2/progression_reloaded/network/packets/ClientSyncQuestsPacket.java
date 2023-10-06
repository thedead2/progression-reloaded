package de.thedead2.progression_reloaded.network.packets;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public class ClientSyncQuestsPacket implements ModNetworkPacket {

    private final Set<ResourceLocation> playerQuests;

    private final Map<ResourceLocation, QuestProgress> questProgress;


    @SuppressWarnings("unused")
    public ClientSyncQuestsPacket(FriendlyByteBuf buf) {
        this.playerQuests = buf.readCollection(Sets::newHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
        this.questProgress = buf.readMap(FriendlyByteBuf::readResourceLocation, QuestProgress::fromNetwork);
    }


    public ClientSyncQuestsPacket(Set<ProgressionQuest> playerQuests, Map<ProgressionQuest, QuestProgress> questProgress) {
        this.playerQuests = (Set<ResourceLocation>) CollectionHelper.convertCollection(playerQuests, new HashSet<>(), ProgressionQuest::getId);
        this.questProgress = CollectionHelper.convertMapKeys(questProgress, ProgressionQuest::getId);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ModClientInstance.getInstance().getClientDataManager().updateQuestProgress(ClientSyncQuestsPacket.this.playerQuests, ClientSyncQuestsPacket.this.questProgress);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.playerQuests, FriendlyByteBuf::writeResourceLocation);
        buf.writeMap(this.questProgress, FriendlyByteBuf::writeResourceLocation, (buf2, progress) -> progress.toNetwork(buf2));
    }
}
