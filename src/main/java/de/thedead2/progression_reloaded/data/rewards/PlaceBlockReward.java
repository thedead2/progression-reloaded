package de.thedead2.progression_reloaded.data.rewards;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockReward implements IReward{
    private final BlockState block;

    public PlaceBlockReward(BlockState block) {
        this.block = block;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        player.getLevel().setBlockAndUpdate(player.blockPosition(), block);
    }
}
