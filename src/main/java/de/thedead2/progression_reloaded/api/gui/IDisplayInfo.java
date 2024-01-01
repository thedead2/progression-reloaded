package de.thedead2.progression_reloaded.api.gui;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.api.IProgressable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;


public interface IDisplayInfo<T extends IProgressable<T>> {

    static IDisplayInfo<?> deserializeFromNetwork(FriendlyByteBuf buf) {
        String className = buf.readUtf();
        try {
            Class<?> clazz = Class.forName(className);
            return (IDisplayInfo<?>) clazz.getDeclaredMethod("fromNetwork", FriendlyByteBuf.class).invoke(null, buf);
        }
        catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    default void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getClass().getName());
        this.toNetwork(buf);
    }

    void toNetwork(FriendlyByteBuf buf);

    JsonElement toJson();

    ItemStack icon();

    Component title();

    Component description();

    ResourceLocation id();
}
