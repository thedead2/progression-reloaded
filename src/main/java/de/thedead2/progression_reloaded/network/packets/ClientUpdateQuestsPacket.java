package de.thedead2.progression_reloaded.network.packets;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.client.ClientManager;
import de.thedead2.progression_reloaded.client.data.ClientQuest;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class ClientUpdateQuestsPacket implements ModNetworkPacket {

    private final Set<ClientQuest> playerQuests;

    private final Map<ClientQuest, QuestProgress> questProgress;


    @SuppressWarnings("unused")
    public ClientUpdateQuestsPacket(FriendlyByteBuf buf) {
        this.playerQuests = buf.readCollection(Sets::newHashSetWithExpectedSize, ClientQuest::fromNetwork);
        this.questProgress = buf.readMap(ClientQuest::fromNetwork, QuestProgress::fromNetwork);
    }


    public ClientUpdateQuestsPacket(Set<ProgressionQuest> playerQuests, Map<ProgressionQuest, QuestProgress> questProgress) {
        this.playerQuests = convertToClientQuests(playerQuests);
        this.questProgress = convertToClientQuestProgress(questProgress);
    }


    private static Set<ClientQuest> convertToClientQuests(Set<ProgressionQuest> playerQuests) {
        return playerQuests.stream().map(ClientQuest::fromProgressionQuest).collect(Collectors.toSet());
    }


    private static Map<ClientQuest, QuestProgress> convertToClientQuestProgress(Map<ProgressionQuest, QuestProgress> questProgress) {
        Map<ClientQuest, QuestProgress> clientProgress = new HashMap<>();
        questProgress.forEach((quest, progress) -> clientProgress.put(ClientQuest.fromProgressionQuest(quest), progress));

        return clientProgress;
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientManager.getInstance().getPlayer().getQuests().updateQuests(ClientUpdateQuestsPacket.this.playerQuests, ClientUpdateQuestsPacket.this.questProgress);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(this.playerQuests, (buf1, quest) -> quest.toNetwork(buf1));
        buf.writeMap(this.questProgress, (buf1, quest) -> quest.toNetwork(buf1), (buf2, progress) -> progress.toNetwork(buf2));
    }
}
