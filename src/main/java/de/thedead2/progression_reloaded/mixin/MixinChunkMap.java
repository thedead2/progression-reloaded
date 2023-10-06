package de.thedead2.progression_reloaded.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;


@Mixin(ChunkMap.class)
public class MixinChunkMap {

    @Shadow
    @Final
    ServerLevel level;


    @Inject(at = @At("RETURN"), method = "protoChunkToFullChunk(Lnet/minecraft/server/level/ChunkHolder;)Ljava/util/concurrent/CompletableFuture;", cancellable = true)
    public void onChunkGeneration(ChunkHolder chunkHolder, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {
        /*if(BlockRestrictionManager.generating){
            cir.setReturnValue(cir.getReturnValue().thenApplyAsync(either -> {
                either.left().ifPresent(chunkAccess -> {
                    ModRestrictionManagers.BLOCK_RESTRICTION_MANAGER.onChunkGeneration(chunkAccess, this.level);
                });


                return either;
            }));
        }*/
    }
}
