package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ITriggerPredicate<T> { //TODO: add predicates

    boolean matches(T t, Object... addArgs);

    Map<String, Object> getFields();

    JsonElement toJson();

    Builder<? extends ITriggerPredicate<T>> deconstruct();
    ITriggerPredicate<T> copy();

    static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, name + "_predicate");
    }

    interface Builder<R extends ITriggerPredicate<?>>{
        R build();
    }
}
