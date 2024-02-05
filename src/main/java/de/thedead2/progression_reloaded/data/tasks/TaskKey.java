package de.thedead2.progression_reloaded.data.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.IJsonSerializable;
import de.thedead2.progression_reloaded.api.INbtSerializable;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


public record TaskKey(ResourceLocation questId, ResourceLocation taskId) implements INetworkSerializable, IJsonSerializable, INbtSerializable {

    public static TaskKey fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation questId = buf.readResourceLocation();
        ResourceLocation taskId = buf.readResourceLocation();

        return new TaskKey(questId, taskId);
    }


    public static TaskKey fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ResourceLocation questId = new ResourceLocation(jsonObject.get("quest").getAsString());
        ResourceLocation taskId = new ResourceLocation(jsonObject.get("task").getAsString());

        return new TaskKey(questId, taskId);
    }


    public static TaskKey fromNBT(CompoundTag tag) {
        ResourceLocation questId = new ResourceLocation(tag.getString("quest"));
        ResourceLocation taskId = new ResourceLocation(tag.getString("task"));

        return new TaskKey(questId, taskId);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.questId);
        buf.writeResourceLocation(this.taskId);
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("quest", this.questId.toString());
        jsonObject.addProperty("task", this.taskId.toString());

        return jsonObject;
    }


    @Override
    public Tag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("quest", this.questId.toString());
        tag.putString("task", this.taskId.toString());

        return tag;
    }

}
