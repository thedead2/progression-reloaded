package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TeleportReward implements IReward{


    private final TeleportDestination destination;

    public TeleportReward(TeleportDestination destination) {
        this.destination = destination;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        this.destination.teleportPlayer(player);
    }

    public static class TeleportDestination{
        private final double pX;
        private final double pY;
        private final double pZ;
        private final float yaw;
        private final float pitch;
        private final ServerLevel level;

        public TeleportDestination(double pX, double pY, double pZ, float yaw, float pitch, ServerLevel level) {
            this.pX = pX;
            this.pY = pY;
            this.pZ = pZ;
            this.yaw = yaw;
            this.pitch = pitch;
            this.level = level;
        }

        public void teleportPlayer(ServerPlayer player){
            if(level != null) player.teleportTo(level, pX, pY, pZ, yaw, pitch);
            else player.teleportTo(pX, pY, pZ);
        }
    }
}
