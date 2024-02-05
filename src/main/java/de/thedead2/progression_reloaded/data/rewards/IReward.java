package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.registries.TypeRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.InvocationTargetException;


public interface IReward {

    static IReward createFromJson(JsonElement element) {
        ResourceLocation id = new ResourceLocation(((JsonObject) element).get("id").getAsString());
        Class<IReward> rewardsClass = TypeRegistries.PROGRESSION_REWARDS.get(id);
        try {
            return (IReward) rewardsClass.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, ((JsonObject) element).get("data"));
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static IReward fromNetwork(FriendlyByteBuf buf) {
        return createFromJson(SerializationHelper.convertToJson(buf.readNbt()));
    }

    static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_reward");
    }

    void rewardPlayer(ServerPlayer player);

    default void toNetwork(FriendlyByteBuf buf) {
        buf.writeNbt((CompoundTag) SerializationHelper.convertToNBT(this.saveToJson()));
    }

    default JsonObject saveToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.getId().toString());
        jsonObject.add("data", this.toJson());
        return jsonObject;
    }

    ResourceLocation getId();

    JsonElement toJson();

    /*@OnlyIn(Dist.CLIENT)
    RewardComponent<? extends IReward> getDisplayComponent(Area area);*/
}
