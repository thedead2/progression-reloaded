package de.thedead2.progression_reloaded.network.packages;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
public interface ModNetworkPacket {

    /**
     * Due to the way how classes are compiled this cannot be a lambda, it has to be an anonymous inner class
     * **/
    DistExecutor.SafeRunnable EMPTY_SAFE_RUNNABLE = new DistExecutor.SafeRunnable() {
        @Override
        public void run() {}
    };

    default DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx){
        return EMPTY_SAFE_RUNNABLE;
    }

    default DistExecutor.SafeRunnable onServer(Supplier<NetworkEvent.Context> ctx){
        return EMPTY_SAFE_RUNNABLE;
    }

    void toBytes(FriendlyByteBuf buf);

    static <T extends ModNetworkPacket> T fromBytes(FriendlyByteBuf buf, Class<T> packetClass) {
        try {
            return packetClass.getConstructor(buf.getClass()).newInstance(buf);
        }
        catch (NoSuchMethodException e) {
            CrashHandler.getInstance().handleException("Failed to invoke new Instance of class: " + packetClass.getName() + " -> The needed constructor doesn't exist!", e, Level.ERROR);
            return null;
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            CrashHandler.getInstance().handleException("Failed to invoke new Instance of class: " + packetClass.getName(), e, Level.ERROR);
            return null;
        }
    }
}
