package de.thedead2.progression_reloaded.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.InvocationTargetException;


public interface IProgressInfo {

    static IProgressInfo deserializeFromNetwork(FriendlyByteBuf buf) {
        String className = buf.readUtf();
        try {
            Class<?> clazz = Class.forName(className);
            return (IProgressInfo) clazz.getDeclaredMethod("fromNetwork", FriendlyByteBuf.class).invoke(null, buf);
        }
        catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the progress of this {@link IProgressInfo} in percent
     **/
    float getPercent();

    /**
     * @return true if the progress of this {@link IProgressInfo} is complete
     **/
    boolean isDone();

    /**
     * Saves this {@link IProgressInfo} to a {@link CompoundTag}
     *
     * @return the {@link CompoundTag} containing the data of this {@link IProgressInfo}
     **/
    CompoundTag saveToCompoundTag();

    /**
     * Resets the progress of this {@link IProgressInfo}
     **/
    void reset();

    /**
     * Causes the progress of this {@link IProgressInfo} to be completed
     **/
    void complete();

    default void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getClass().getName());
        this.toNetwork(buf);
    }

    /**
     * Serializes this {@link IProgressInfo} to the network buffer
     *
     * @param buf the {@link FriendlyByteBuf} to write data to
     **/
    void toNetwork(FriendlyByteBuf buf);
}
