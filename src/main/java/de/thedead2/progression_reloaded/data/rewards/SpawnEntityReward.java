package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

public class SpawnEntityReward implements IReward{

    private final EntityType<?> entityType;
    private final int amount;
    public SpawnEntityReward(EntityType<?> entityType, int amount){
        this.entityType = entityType;
        this.amount = amount;
    }

    public SpawnEntityReward(EntityType<?> entityType){
        this(entityType, 1);
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        for (int i = 0; i < amount; i++)
            entityType.spawn(player.getLevel(), null, player, player.blockPosition(), MobSpawnType.COMMAND, true, false);
    }
}
