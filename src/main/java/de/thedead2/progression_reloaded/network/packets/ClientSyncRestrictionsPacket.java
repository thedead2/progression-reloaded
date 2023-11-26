package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.IRestrictionType;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.data.restrictions.Restriction;
import de.thedead2.progression_reloaded.data.restrictions.RestrictionKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class ClientSyncRestrictionsPacket implements ModNetworkPacket {

    private final Map<IRestrictionType<?>, Map<RestrictionKey<?>, Restriction<?>>> restrictions;


    public ClientSyncRestrictionsPacket(Map<IRestrictionType<?>, Map<RestrictionKey<?>, Restriction<?>>> restrictions) {
        this.restrictions = restrictions;
    }


    @SuppressWarnings("unused")
    public ClientSyncRestrictionsPacket(FriendlyByteBuf buf) {
        this.restrictions = buf.readMap(IRestrictionType::fromNetwork, buf1 -> buf1.readMap(RestrictionKey::fromNetwork, Restriction::deserializeFromNetwork));
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ModClientInstance.getInstance().getClientRestrictionManager().acceptSync(ClientSyncRestrictionsPacket.this.restrictions);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeMap(this.restrictions, (buf1, restrictionType) -> restrictionType.toNetwork(buf1), (buf1, restrictions) -> buf1.writeMap(restrictions, (buf2, key) -> key.toNetwork(buf2), (buf2, restriction) -> restriction.serializeToNetwork(buf2)));
    }
}
