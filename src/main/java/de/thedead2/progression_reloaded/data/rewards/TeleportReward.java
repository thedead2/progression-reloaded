package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


public class TeleportReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("teleport");

    private final TeleportDestination destination;


    public TeleportReward(TeleportDestination destination) {
        this.destination = destination;
    }


    public static TeleportReward fromJson(JsonElement jsonObject) {
        return new TeleportReward(TeleportDestination.fromJson(jsonObject.getAsJsonObject()));
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        this.destination.teleportPlayer(player);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        return this.destination.toJson();
    }


    public static class TeleportDestination {

        private final double pX;

        private final double pY;

        private final double pZ;

        private final float yaw;

        private final float pitch;

        private final ResourceKey<Level> level;


        public TeleportDestination(double pX, double pY, double pZ, float yaw, float pitch, ResourceKey<Level> level) {
            this.pX = pX;
            this.pY = pY;
            this.pZ = pZ;
            this.yaw = yaw;
            this.pitch = pitch;
            this.level = level;
        }


        public static TeleportDestination fromJson(JsonObject jsonObject) {
            double pX, pY, pZ;
            float yaw, pitch;
            ResourceKey<Level> level = null;
            pX = jsonObject.get("x").getAsDouble();
            pY = jsonObject.get("y").getAsDouble();
            pZ = jsonObject.get("z").getAsDouble();
            /*yaw = jsonObject.get("yaw").getAsFloat();
            pitch = jsonObject.get("pitch").getAsFloat();
            */
            if(jsonObject.has("level")) {
                level = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(jsonObject.get("level").getAsString()));
            }

            return new TeleportDestination(pX, pY, pZ, 0, 0, level);
        }


        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("x", this.pX);
            jsonObject.addProperty("y", this.pY);
            jsonObject.addProperty("z", this.pZ);
            /*jsonObject.addProperty("yaw", this.yaw);
            jsonObject.addProperty("pitch", this.pitch);
            */
            if(level != null) {
                jsonObject.addProperty("level", this.level.location().toString());
            }

            return jsonObject;
        }


        public void teleportPlayer(ServerPlayer player) {
            if(level != null) {
                player.teleportTo(player.getServer().getLevel(level), pX, pY, pZ, yaw, pitch);
            }
            else {
                player.teleportTo(pX, pY, pZ);
            }
        }
    }
}
