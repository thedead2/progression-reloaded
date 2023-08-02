package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockReward implements IReward{
    public static final ResourceLocation ID = IReward.createId("place_block");
    private final BlockState block;

    public PlaceBlockReward(BlockState block) {
        this.block = block;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        player.getLevel().setBlockAndUpdate(player.blockPosition(), block);
    }

    public static PlaceBlockReward fromJson(JsonElement jsonElement){
        BlockState state = Block.stateById(jsonElement.getAsInt());
        return new PlaceBlockReward(state);
    }
    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(Block.getId(this.block));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
