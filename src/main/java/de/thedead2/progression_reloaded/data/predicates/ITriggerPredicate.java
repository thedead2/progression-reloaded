package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;


public interface ITriggerPredicate<T> {

    static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_predicate");
    }

    boolean matches(T t, Object... addArgs);

    JsonElement toJson();

    interface Builder<R extends ITriggerPredicate<?>> {

        R build();
    }
}
