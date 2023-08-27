package de.thedead2.progression_reloaded.data.abilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;

public interface IAbility<T> {
    static IAbility<?> createFromJson(JsonElement element){
        ResourceLocation id = new ResourceLocation(((JsonObject) element).get("id").getAsString());
        Class<IAbility<?>> abilityClass = DynamicRegistries.PROGRESSION_ABILITIES.get(id);
        try {
            return (IAbility<?>) abilityClass.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, ((JsonObject) element).get("data"));
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }

    ResourceLocation getId();
    boolean isPlayerAbleTo(T t);

    static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, name + "_ability");
    }

    JsonElement toJson();
}
