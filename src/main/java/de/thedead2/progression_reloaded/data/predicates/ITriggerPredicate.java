package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ITriggerPredicate<T> {

    boolean matches(T t, Object... addArgs);

    JsonElement toJson();

    static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, name + "_predicate");
    }

    interface Builder<R extends ITriggerPredicate<?>>{
        R build();
    }
}
