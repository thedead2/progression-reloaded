package de.thedead2.progression_reloaded.client.data;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import net.minecraft.network.FriendlyByteBuf;


public class ClientLevelProgress {

    private final float percent;

    private final boolean hasBeenRewarded;


    public ClientLevelProgress(float percent, boolean hasBeenRewarded) {
        this.percent = percent;
        this.hasBeenRewarded = hasBeenRewarded;
    }


    public static ClientLevelProgress fromLevelProgress(LevelProgress progress, KnownPlayer player) {
        return new ClientLevelProgress(progress.getPercent(player), progress.hasBeenRewarded(player));
    }


    public static ClientLevelProgress fromNetwork(FriendlyByteBuf buf) {
        float percent = buf.readFloat();
        boolean hasBeenRewarded = buf.readBoolean();

        return new ClientLevelProgress(percent, hasBeenRewarded);
    }


    public float getPercent() {
        return percent;
    }


    public boolean hasBeenRewarded() {
        return hasBeenRewarded;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.percent);
        buf.writeBoolean(this.hasBeenRewarded);
    }
}
