package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;


public interface ModNetworkPacket {

    /**
     * Due to the way how classes are compiled this cannot be a lambda, it has to be an inner class
     **/
    @SuppressWarnings("Convert2Lambda")
    DistExecutor.SafeRunnable EMPTY_SAFE_RUNNABLE = new DistExecutor.SafeRunnable() {
        @Override
        public void run() {
        }
    };

    static <T extends ModNetworkPacket> T fromBytes(FriendlyByteBuf buf, Class<T> packetClass) {
        try {
            return packetClass.getConstructor(buf.getClass()).newInstance(buf);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException |
              NoSuchMethodException e) {
            CrashHandler.getInstance().handleException("Failed to invoke new Instance of class: " + packetClass.getName(), e, Level.ERROR);

            return null;
        }
    }

    default DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return EMPTY_SAFE_RUNNABLE;
    }

    default DistExecutor.SafeRunnable onServer(Supplier<NetworkEvent.Context> ctx) {
        return EMPTY_SAFE_RUNNABLE;
    }

    void toBytes(FriendlyByteBuf buf);
}
