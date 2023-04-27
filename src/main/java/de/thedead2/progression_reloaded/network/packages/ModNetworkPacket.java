package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;
public abstract class ModNetworkPacket {

    protected ModNetworkPacket(){}
    protected ModNetworkPacket(FriendlyByteBuf buf){}

    public void onClient(Supplier<NetworkEvent.Context> ctx){
        LOGGER.warn("Told to do something on the client but I don't know what! -> {}", this.getClass().getName());
    }

    public void onServer(Supplier<NetworkEvent.Context> ctx){
        LOGGER.warn("Told to do something on the server but I don't know what! -> {} from {}", this.getClass().getName(), ctx.get().getSender().getName().getString());
    }

    public void toBytes(FriendlyByteBuf buf){}

    public static <T extends ModNetworkPacket> T fromBytes(FriendlyByteBuf buf, Class<T> packetClass) {
        try {
            return packetClass.getConstructor(buf.getClass()).newInstance(buf);
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            CrashHandler.getInstance().handleException("Failed to invoke new Instance of class: " + packetClass.getName(), e, Level.ERROR, true);
            return null;
        }
    }
}
