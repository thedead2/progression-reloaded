package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public interface ITriggerPredicate<T> {

    static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_predicate");
    }

    boolean matches(T t, Object... addArgs);

    JsonElement toJson();

    default void toNetwork(FriendlyByteBuf buf) {
        buf.writeNbt((CompoundTag) SerializationHelper.convertToNBT(this.toJson()));
    }

    Component getDefaultDescription();
}
