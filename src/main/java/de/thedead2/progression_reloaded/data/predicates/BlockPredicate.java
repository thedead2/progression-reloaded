package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class BlockPredicate implements ITriggerPredicate<BlockState> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("block");

    public static BlockPredicate fromJson(JsonElement block) {
        return null;
    }

    @Override
    public boolean matches(BlockState blockState, Object... addArgs) {
        return false;
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public Builder<BlockPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<BlockState> copy() {
        return null;
    }
    public static BlockPredicate from(BlockState blockState) {
        return null;
    }

}
