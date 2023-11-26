package de.thedead2.progression_reloaded.api;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


public interface IProgressable<T extends IProgressable<T>> extends Comparable<T> {
    JsonElement toJson();
    ResourceLocation getId();

    Component getTitle();

    default boolean hasParent() {
        return this.getParent() != null;
    }

    @Nullable
    ResourceLocation getParent();
}
