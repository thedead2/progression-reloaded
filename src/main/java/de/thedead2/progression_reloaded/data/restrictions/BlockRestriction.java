package de.thedead2.progression_reloaded.data.restrictions;

import de.thedead2.progression_reloaded.data.RestrictionManager;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class BlockRestriction extends Restriction<Block> {

    private final ReplacementMode blockReplacementMode;

    @Nullable
    private final ResourceLocation modelReplacement;


    public BlockRestriction(@NotNull ResourceLocation levelId, ReplacementMode blockReplacementMode, @Nullable ResourceLocation modelReplacement) {
        super(levelId);
        this.blockReplacementMode = blockReplacementMode;
        this.modelReplacement = modelReplacement;
    }


    protected static BlockRestriction fromNetwork(FriendlyByteBuf buf, ResourceLocation levelId) {
        ReplacementMode replacementMode = buf.readEnum(ReplacementMode.class);
        ResourceLocation replacement = buf.readNullable(FriendlyByteBuf::readResourceLocation);

        return new BlockRestriction(levelId, replacementMode, replacement);
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(this.blockReplacementMode);
        buf.writeNullable(this.modelReplacement, FriendlyByteBuf::writeResourceLocation);
    }


    public ReplacementMode getBlockReplacementMode() {
        return blockReplacementMode;
    }


    @Nullable
    public ResourceLocation getReplacement() {
        return this.modelReplacement;
    }


    public enum ReplacementMode {
        STATIC {
            @Override
            @Nullable
            public Pair<BlockPos, BlockState> computeReplacement(RestrictionManager manager, ChunkAccess chunk, BlockPos blockPos, BlockState blockToCheck) {
                BlockRestriction restriction = manager.getRestrictionFor(RestrictionTypes.BLOCK, BlockRestriction.class, blockToCheck.getBlock());
                if(restriction == null) {
                    return null;
                }

                Block replacementBlock = ForgeRegistries.BLOCKS.getValue(restriction.getReplacement());
                if(replacementBlock != null) {
                    return Pair.of(blockPos, replacementBlock.defaultBlockState());
                }
                return null;
            }
        },
        SURROUNDING {
            @Override
            @Nullable
            public Pair<BlockPos, BlockState> computeReplacement(RestrictionManager manager, ChunkAccess chunk, BlockPos blockPos, BlockState blockToCheck) {
                int areaDimensions = 5; //Check 5 blocks around

                AABB areaToCheck = AABB.ofSize(blockPos.getCenter(), areaDimensions, areaDimensions, areaDimensions);
                List<BlockState> surroundingBlocks = chunk.getBlockStates(areaToCheck).filter(blockState -> !blockState.isAir() && !blockState.equals(blockToCheck) && !manager.isRestricted(RestrictionTypes.BLOCK, blockState.getBlock())).toList();
                BlockState repl = CollectionHelper.findObjectWithHighestCount(surroundingBlocks).orElse(null);
                if(repl != null) {
                    return Pair.of(blockPos, repl);
                }

                return null;
            }
        };


        @Nullable
        public abstract Pair<BlockPos, BlockState> computeReplacement(RestrictionManager manager, ChunkAccess chunk, BlockPos blockPos, BlockState blockToCheck);
    }
}
