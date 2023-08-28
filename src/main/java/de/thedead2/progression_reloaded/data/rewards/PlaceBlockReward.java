package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;


public class PlaceBlockReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("place_block");

    private final Block block;


    public PlaceBlockReward(Block block) {
        this.block = block;
    }


    public static PlaceBlockReward fromJson(JsonElement jsonElement) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(GsonHelper.convertToString(jsonElement, "block")));

        return new PlaceBlockReward(block);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        var blockState = this.block.defaultBlockState();
        player.getLevel().setBlockAndUpdate(player.blockPosition(), blockState);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(ForgeRegistries.BLOCKS.getKey(block).toString());
    }
}
