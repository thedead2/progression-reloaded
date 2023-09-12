package de.thedead2.progression_reloaded.api.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public interface IProgressInfo {

    float getPercent();

    boolean isDone();

    void toNetwork(FriendlyByteBuf buf);

    CompoundTag saveToCompoundTag();

    void reset();

    void complete();
}
