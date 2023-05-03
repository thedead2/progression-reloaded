package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.network.packages.ClientOpenProgressionBookPacket;
import de.thedead2.progression_reloaded.network.packages.ModNetworkPacket;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
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

    private static int messageId = 0;


    private static int messageId(){
        return messageId++;
    }

    public static void registerPackets(){
        LOGGER.debug("Registering network packages...");

        registerPacket(ClientOpenProgressionBookPacket.class);

        LOGGER.debug("Network registration complete.");
    }

    private static <T extends ModNetworkPacket> void registerPacket(Class<T> packet){
        INSTANCE.messageBuilder(packet, messageId(), getDirection(packet)).decoder(buf -> ModNetworkPacket.fromBytes(buf, packet)).encoder(ModNetworkPacket::toBytes).consumerMainThread(ModNetworkHandler::handlePacket).add();
    }

    private static <T extends ModNetworkPacket> NetworkDirection getDirection(Class<T> packet){
        Set<String> methodNames = new HashSet<>();
        String clazzName = packet.getName();
        for (Method declaredMethod : packet.getDeclaredMethods()) {
            if(declaredMethod.getName().equals("onClient") || declaredMethod.getName().equals("onServer")){
                methodNames.add(declaredMethod.getName());
            }
        }
        if(methodNames.containsAll(Set.of("onClient", "onServer"))) return null;
        else if (methodNames.contains("onClient")){
            if (clazzName.contains("Login")) return NetworkDirection.LOGIN_TO_CLIENT;
            else return NetworkDirection.PLAY_TO_CLIENT;
        }
        else if (methodNames.contains("onServer")) {
            if (clazzName.contains("Login")) return NetworkDirection.LOGIN_TO_SERVER;
            else return NetworkDirection.PLAY_TO_SERVER;
        }
        else return null;
    }

    public static void handlePacket(ModNetworkPacket packet, Supplier<NetworkEvent.Context> ctx){
        try {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> packet.onClient(ctx));
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> packet.onServer(ctx));
            ctx.get().setPacketHandled(true);
        }
        catch (Throwable throwable){
            CrashHandler.getInstance().handleException("Failed to handle network packet -> " + packet.getClass().getName(), "NetworkHandler", throwable, Level.ERROR);
            ctx.get().setPacketHandled(false);
        }
    }

    public static <MSG extends ModNetworkPacket> void sendToServer(MSG msg){
        INSTANCE.sendToServer(msg);
    }

    public static <MSG extends ModNetworkPacket> void sendToPlayer(MSG msg, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
