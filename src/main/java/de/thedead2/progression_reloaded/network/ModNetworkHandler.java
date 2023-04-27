package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.network.packages.ClientOpenProgressionBookPacket;
import de.thedead2.progression_reloaded.network.packages.ModNetworkPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public abstract class ModNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets(){
        int id = 0;
        LOGGER.debug("Registering network packages...");

        newPacket(ClientOpenProgressionBookPacket.class, id++);

        LOGGER.debug("Network registration complete.");
    }

    private static <T extends ModNetworkPacket> void newPacket(Class<T> packet, int id){
    newPacket(packet, buf -> ModNetworkPacket.fromBytes(buf, packet), id);
    }

    private static <T extends ModNetworkPacket> void newPacket(Class<T> packet, Function<FriendlyByteBuf, T> decoder, int id){
        INSTANCE.messageBuilder(packet, id).decoder(decoder).encoder(ModNetworkPacket::toBytes).consumerMainThread(ModNetworkHandler::handlePacket).add();
    }

    private static void handlePacket(ModNetworkPacket packet, Supplier<NetworkEvent.Context> ctx){
        if(FMLEnvironment.dist.isClient()) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packet.onClient(ctx)));
        }
        else {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> packet.onServer(ctx)));
        }
        ctx.get().setPacketHandled(true);
    }

    public static <MSG extends ModNetworkPacket> void sendToServer(MSG msg){
        INSTANCE.sendToServer(msg);
    }

    public static <MSG extends ModNetworkPacket> void sendToPlayer(MSG msg, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
