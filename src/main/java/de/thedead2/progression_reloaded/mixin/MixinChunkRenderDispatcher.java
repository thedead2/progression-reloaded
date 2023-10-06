package de.thedead2.progression_reloaded.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public class MixinChunkRenderDispatcher {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;")
    public BlockState whileCompilingBlockState(RenderChunkRegion instance, BlockPos pos) {
        return instance.getBlockState(pos);
        //return ModRestrictionManagers.BLOCK_RESTRICTION_MANAGER.getReplacementIfPresent(instance.getBlockState(setPoint), setPoint, Minecraft.getInstance().player, Minecraft.getInstance().level);
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"), method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;")
    public BlockEntity whileCompilingBlockEntity(RenderChunkRegion instance, BlockPos pos) {
        return instance.getBlockEntity(pos);
        //return BlockRestrictionManager.ClientExtension.checkForRestrictions(, setPoint);
    }
}
