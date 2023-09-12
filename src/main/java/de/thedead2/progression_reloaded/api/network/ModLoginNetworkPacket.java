package de.thedead2.progression_reloaded.api.network;

import java.util.function.IntSupplier;


public interface ModLoginNetworkPacket extends ModNetworkPacket, IntSupplier {

    void setLoginIndex(int i);
}
