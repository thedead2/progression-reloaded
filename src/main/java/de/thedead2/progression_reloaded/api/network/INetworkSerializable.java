package de.thedead2.progression_reloaded.api.network;

import net.minecraft.network.FriendlyByteBuf;


public interface INetworkSerializable {

    void toNetwork(FriendlyByteBuf buf);
}
