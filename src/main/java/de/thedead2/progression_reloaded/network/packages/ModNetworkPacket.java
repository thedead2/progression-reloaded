package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
public interface ModNetworkPacket {

    DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx);

    DistExecutor.SafeRunnable onServer(Supplier<NetworkEvent.Context> ctx);

    void toBytes(FriendlyByteBuf buf);

    static <T extends ModNetworkPacket> T fromBytes(FriendlyByteBuf buf, Class<T> packetClass) {
        try {
            return packetClass.getConstructor(buf.getClass()).newInstance(buf);
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            CrashHandler.getInstance().handleException("Failed to invoke new Instance of class: " + packetClass.getName(), e, Level.ERROR, true);
            return null;
        }
    }
}
