package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ServerFollowQuestPacket implements ModNetworkPacket {

    private final ResourceLocation questId;


    public ServerFollowQuestPacket(ResourceLocation questId) {
        this.questId = questId;
    }


    @SuppressWarnings("unused")
    public ServerFollowQuestPacket(FriendlyByteBuf buf) {
        this.questId = buf.readNullable(FriendlyByteBuf::readResourceLocation);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onServer(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                PlayerData player = PlayerDataManager.getPlayerData(ctx.get().getSender());

                player.getPlayerQuests().followQuest(ServerFollowQuestPacket.this.questId);
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNullable(this.questId, FriendlyByteBuf::writeResourceLocation);
    }
}
