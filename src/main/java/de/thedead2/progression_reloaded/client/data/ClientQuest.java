package de.thedead2.progression_reloaded.client.data;

import de.thedead2.progression_reloaded.client.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.client.display.RewardsDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;


public class ClientQuest {

    private final QuestDisplayInfo displayInfo;

    private final RewardsDisplayInfo rewards;


    public ClientQuest(QuestDisplayInfo displayInfo, RewardsDisplayInfo rewards) {
        this.displayInfo = displayInfo;
        this.rewards = rewards;
    }


    public static ClientQuest fromNetwork(FriendlyByteBuf buf) {
        QuestDisplayInfo displayInfo = buf.readNullable(QuestDisplayInfo::fromNetwork);
        RewardsDisplayInfo rewards = buf.readNullable(RewardsDisplayInfo::fromNetwork);

        return new ClientQuest(displayInfo, rewards);
    }


    public static ClientQuest fromProgressionQuest(ProgressionQuest quest) {
        return new ClientQuest(quest.getDisplay(), quest.getRewards().getDisplay());
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNullable(this.displayInfo, (buf1, questDisplayInfo) -> questDisplayInfo.toNetwork(buf1));
        buf.writeNullable(this.rewards, (buf1, rewardsDisplayInfo) -> rewardsDisplayInfo.toNetwork(buf1));
    }


    public QuestDisplayInfo getDisplayInfo() {
        return displayInfo;
    }


    public Component getTitle() {
        return this.displayInfo.getTitle();
    }


    public RewardsDisplayInfo getRewards() {
        return rewards;
    }
}
