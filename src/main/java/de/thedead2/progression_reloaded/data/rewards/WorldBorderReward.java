package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;


public class WorldBorderReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("world_border");

    private final double addWidth;

    private final long timeInMillis;


    public WorldBorderReward(double addWidth, long timeInMillis) {
        this.addWidth = addWidth;
        this.timeInMillis = timeInMillis;
    }


    public static WorldBorderReward fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        double addWidth = jsonObject.get("add_width").getAsDouble();
        long timeInTicks = jsonObject.get("time").getAsLong();

        return new WorldBorderReward(addWidth, timeInTicks);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        WorldBorder worldborder = player.getServer().overworld().getWorldBorder();
        double oldSize = worldborder.getSize();
        double newSize = oldSize + this.addWidth;

        if(oldSize >= 5.9999968E7D) {
            return;
        }

        if(newSize < 1.0D || newSize > 5.9999968E7D) {
            ModHelper.LOGGER.warn("Can't change world border size as the resulting size would be too {}!", newSize < 1.0D ? "small" : "big");
            return;
        }

        if(this.timeInMillis > 0L) {
            worldborder.lerpSizeBetween(oldSize, newSize, this.timeInMillis);
            if(newSize > oldSize) {
                player.sendSystemMessage(Component.literal("Seems like the world border is increasing..."));
            }
            else {
                player.sendSystemMessage(Component.literal("Seems like the world border is shrinking..."));
            }
        }
        else {
            worldborder.setSize(newSize);
            if(newSize > oldSize) {
                player.sendSystemMessage(Component.literal("Seems like the world border has been increased..."));
            }
            else {
                player.sendSystemMessage(Component.literal("Seems like the world border has been shrunk..."));
            }
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("add_width", this.addWidth);
        jsonObject.addProperty("time", this.timeInMillis);

        return jsonObject;
    }
}
