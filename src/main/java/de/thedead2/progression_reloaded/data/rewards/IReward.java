package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public interface IReward {
    void rewardPlayer(ServerPlayer player);

    JsonElement toJson();

    ResourceLocation getId();

    static IReward createFromJson(JsonElement element){
        ResourceLocation id = new ResourceLocation(((JsonObject) element).get("id").getAsString());
        Class<IReward> rewardsClass = DynamicRegistries.PROGRESSION_REWARDS.get(id);
        try {
            return (IReward) rewardsClass.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, ((JsonObject) element).get("data"));
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }

    static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, name + "_reward");
    }

    default JsonElement saveToJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.getId().toString());
        jsonObject.add("data", this.toJson());
        return jsonObject;
    }
}
