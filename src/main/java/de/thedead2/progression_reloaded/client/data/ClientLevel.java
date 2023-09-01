package de.thedead2.progression_reloaded.client.data;

import de.thedead2.progression_reloaded.client.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.client.display.RewardsDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;


public class ClientLevel {

    private final LevelDisplayInfo displayInfo;

    private final RewardsDisplayInfo rewards;


    public ClientLevel(LevelDisplayInfo displayInfo, RewardsDisplayInfo rewards) {
        this.displayInfo = displayInfo;
        this.rewards = rewards;
    }


    public static ClientLevel fromNetwork(FriendlyByteBuf buf) {
        LevelDisplayInfo displayInfo = buf.readNullable(LevelDisplayInfo::fromNetwork);
        RewardsDisplayInfo rewards = buf.readNullable(RewardsDisplayInfo::fromNetwork);

        return new ClientLevel(displayInfo, rewards);
    }


    public static ClientLevel fromProgressionLevel(ProgressionLevel level) {
        return new ClientLevel(level.getDisplay(), level.getRewards().getDisplay());
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNullable(this.displayInfo, (buf1, levelDisplayInfo) -> levelDisplayInfo.toNetwork(buf1));
        buf.writeNullable(this.rewards, (buf1, rewardsDisplayInfo) -> rewardsDisplayInfo.toNetwork(buf1));
    }


    public LevelDisplayInfo getDisplayInfo() {
        return displayInfo;
    }


    public Component getTitle() {
        return this.displayInfo.getTitle();
    }


    public RewardsDisplayInfo getRewards() {
        return rewards;
    }
}
