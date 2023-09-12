package de.thedead2.progression_reloaded.network;

import de.thedead2.progression_reloaded.api.network.ModLoginNetworkPacket;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.network.packets.*;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public abstract class ModNetworkHandler {

    private static final Marker marker = new MarkerManager.Log4jMarker("NetworkHandler");

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int messageId = 0;


    public static void registerPackets() {
        LOGGER.debug(marker, "Registering network packets...");

        registerPacket(ClientOpenProgressionBookPacket.class);
        registerPacket(ClientUsedExtraLifePacket.class);
        registerPacket(ClientSyncQuestsPacket.class);
        registerPacket(ClientSyncLevelsPacket.class);
        registerPacket(ClientSyncRestrictionsPacket.class);
        registerPacket(ClientSyncPlayerPacket.class);

        LOGGER.debug(marker, "Network registration complete.");
    }


    private static <T extends ModNetworkPacket> void registerPacket(Class<T> packet) {
        try {
            boolean loginPacket = false;
            packet.getConstructor(FriendlyByteBuf.class);
            for(Class<?> anInterface : packet.getInterfaces()) {
                if(anInterface.getName().equals(ModLoginNetworkPacket.class.getName())) {
                    loginPacket = true;
                    break;
                }
            }
            NetworkDirection direction = getDirection(packet, loginPacket);

            if(loginPacket) {
                INSTANCE.messageBuilder(packet, nextMessageId(), direction)
                        .markAsLoginPacket()
                        .loginIndex(t -> ((ModLoginNetworkPacket) t).getAsInt(), (t, integer) -> ((ModLoginNetworkPacket) t).setLoginIndex(integer))
                        .decoder(buf -> ModNetworkPacket.fromBytes(buf, packet))
                        .encoder(ModNetworkPacket::toBytes)
                        .consumerMainThread(ModNetworkHandler::handlePacket)
                        .add();
            }
            else {
                INSTANCE.messageBuilder(packet, nextMessageId(), direction)
                        .decoder(buf -> ModNetworkPacket.fromBytes(buf, packet))
                        .encoder(ModNetworkPacket::toBytes)
                        .consumerMainThread(ModNetworkHandler::handlePacket)
                        .add();
            }

        }
        catch(Throwable e) {
            CrashHandler.getInstance().handleException("Failed to register ModNetworkPacket: " + packet.getName(), "ModNetworkHandler", e, Level.ERROR);
        }
    }


    private static int nextMessageId() {
        return messageId++;
    }


    private static <T extends ModNetworkPacket> NetworkDirection getDirection(Class<T> packet, boolean loginPacket) {
        Set<String> methodNames = new HashSet<>();
        for(Method declaredMethod : packet.getDeclaredMethods()) {
            if(declaredMethod.getName().equals("onClient") || declaredMethod.getName().equals("onServer")) {
                methodNames.add(declaredMethod.getName());
            }
        }
        if(methodNames.containsAll(Set.of("onClient", "onServer"))) {
            return null;
        }
        else if(methodNames.contains("onClient")) {
            if(loginPacket) {
                return NetworkDirection.LOGIN_TO_CLIENT;
            }
            else {
                return NetworkDirection.PLAY_TO_CLIENT;
            }
        }
        else if(methodNames.contains("onServer")) {
            if(loginPacket) {
                return NetworkDirection.LOGIN_TO_SERVER;
            }
            else {
                return NetworkDirection.PLAY_TO_SERVER;
            }
        }
        else {
            return null;
        }
    }


    private static void handlePacket(ModNetworkPacket packet, Supplier<NetworkEvent.Context> ctx) {
        try {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> {
                LOGGER.debug(marker, "Received packet {} from server, attempting to handle it...", packet.getClass().getName());
                return packet.onClient(ctx);
            });
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> {
                LOGGER.debug(marker, "Received packet {} from player {}, attempting to handle it...", packet.getClass().getName(), ctx.get().getSender().getUUID());
                return packet.onServer(ctx);
            });
            ctx.get().setPacketHandled(true);
            LOGGER.debug(marker, "Handled packet {} successfully...", packet.getClass().getName());
        }
        catch(Throwable throwable) {
            CrashHandler.getInstance().handleException("Failed to handle network packet -> " + packet.getClass().getName(), marker, throwable, Level.FATAL);
            ctx.get().setPacketHandled(false);
        }
    }


    public static <MSG extends ModNetworkPacket> void sendToServer(@NotNull MSG msg) {
        INSTANCE.sendToServer(msg);
    }


    @SuppressWarnings("ConstantValue")
    public static <MSG extends ModNetworkPacket> void sendToPlayer(@NotNull MSG msg, @NotNull ServerPlayer player) {
        if(player.connection == null) {
            LOGGER.error(marker, "Can't send packet {} to player {} as the connection is null!", msg.getClass().getName(), player.getName().getString());
            return;
        }
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
