package de.thedead2.progression_reloaded.client.display;

import net.minecraft.network.FriendlyByteBuf;


public class RewardsDisplayInfo {

    public static RewardsDisplayInfo fromNetwork(FriendlyByteBuf buf) {
        return new RewardsDisplayInfo();
    }


    public void toNetwork(FriendlyByteBuf buf) {

    }
}
