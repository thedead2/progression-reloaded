package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.data.AbilityManager;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.data.abilities.restrictions.Restriction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class ClientSyncRestrictionsPacket implements ModNetworkPacket {

    private final ResourceLocation managerId;

    private final boolean shouldReset;

    private final DefaultAction defaultAction;

    private final Map<RestrictionKey<Object>, Restriction<Object>> restrictions;


    public ClientSyncRestrictionsPacket(ResourceLocation managerId, boolean shouldReset, DefaultAction defaultAction, Map<RestrictionKey<Object>, Restriction<Object>> restrictions) {
        this.managerId = managerId;
        this.shouldReset = shouldReset;
        this.defaultAction = defaultAction;
        this.restrictions = restrictions;
    }


    @SuppressWarnings("unused")
    public ClientSyncRestrictionsPacket(FriendlyByteBuf buf) {
        this.managerId = buf.readResourceLocation();
        this.shouldReset = buf.readBoolean();
        this.defaultAction = buf.readEnum(DefaultAction.class);
        this.restrictions = buf.readMap(RestrictionKey::fromNetwork, Restriction::deserializeFromNetwork);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                var restrictionManager = AbilityManager.getManagerForId(ClientSyncRestrictionsPacket.this.managerId);

                if(ClientSyncRestrictionsPacket.this.shouldReset) {
                    restrictionManager.reset();
                    return;
                }
                restrictionManager.syncRestrictions(ClientSyncRestrictionsPacket.this.restrictions, ClientSyncRestrictionsPacket.this.defaultAction);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.managerId);
        buf.writeBoolean(this.shouldReset);
        buf.writeEnum(this.defaultAction);
        buf.writeMap(this.restrictions, (buf1, key) -> key.toNetwork(buf1), (buf1, restriction) -> restriction.serializeToNetwork(buf1));
    }
}
