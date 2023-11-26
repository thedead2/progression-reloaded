/*
package de.thedead2.progression_reloaded.data.restrictions.managers;

import com.mojang.serialization.Codec;
import de.thedead2.progression_reloaded.data.restrictions.BlockRestriction;
import de.thedead2.progression_reloaded.events.LevelEvent;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.UpdateRenderersPacket;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static de.thedead2.progression_reloaded.util.ModHelper.*;
import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


public class Temp {
    private static final BlockState defaultReplacement = Blocks.AIR.defaultBlockState();
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, defaultReplacement);


    */
/**
 * Map of {@link ChunkPos} as longs to a {@link LevelChunkExtender} representing a chunk, containing the calculated replacement block states for each {@link BlockPos}
 *//*

    private final ConcurrentHashMap<Long, LevelChunkExtender> blockReplacementCache;


    public Temp(ConcurrentHashMap<Long, LevelChunkExtender> blockReplacementCache) {this.blockReplacementCache = blockReplacementCache;}


    //We're loading and unloading the block replacement data together with the corresponding chunk load and unload
    @SubscribeEvent
    public void onChunkDataLoad(final ChunkDataEvent.Load event){
        LOGGER.debug("On load: {}", this.blockReplacementCache.size());
        CompoundTag chunkData = event.getData();
        ChunkAccess chunk = event.getChunk();
        ChunkPos chunkPos = chunk.getPos();
        if(chunkData.contains(MOD_ID + "_block_replacements")){
            this.blockReplacementCache.put(chunkPos.toLong(), LevelChunkExtender.fromCompoundTag(chunkData.getCompound(MOD_ID + "_block_replacements")));
        }
    }

    public LevelChunkExtender checkChunkForRestrictedBlocks(ChunkAccess chunk){
        final long startTime = System.nanoTime();
        AtomicInteger counter = new AtomicInteger();
        ChunkPos chunkPos = chunk.getPos();
        LOGGER.debug("Starting to check chunk {} for block restrictions...", chunkPos.toString());
        final LevelChunkSection[] chunkSections = chunk.getSections();
        final Long2ObjectOpenHashMap<LevelChunkSectionExtender> sections = new Long2ObjectOpenHashMap<>(chunkSections.length);
        BlockPos chunkOrigin = chunkPos.getWorldPosition();
        for(LevelChunkSection chunkSection : chunkSections){
            if(chunkSection.hasOnlyAir()) continue; //We don't need to check air blocks for restrictions

            var section = sections.computeIfAbsent(chunkSection.bottomBlockY(), value ->  new LevelChunkSectionExtender((int) value)); //TODO: ?
            BlockPos sectionOrigin = chunkOrigin.atY(chunkSection.bottomBlockY());

            //We're checking every block inside the chunk section for restrictions
            for(BlockPos blockPos : BlockPos.betweenClosed(sectionOrigin, sectionOrigin.offset(15, 15, 15))) {
                blockPos = blockPos.immutable();
                BlockState blockToCheck = chunkSection.getBlockState(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);

                if(!blockToCheck.isAir() && !hasBeenCached(chunkPos, blockPos)) {
                    Pair<Boolean, BlockRestriction> pair = this.isRestricted(blockToCheck.getBlock());

                    if(pair.getLeft()) {
                        BlockRestriction restriction = pair.getRight();

                        final BlockPos finalBlockPos = blockPos;
                        switch(restriction.getBlockReplacementMode()) {
                            case STATIC -> {
                                ResourceLocation replacementId = restriction.getReplacement();
                                Block replacementBlock = ForgeRegistries.BLOCKS.getValue(replacementId);
                                if(replacementBlock != null) {
                                    section.setBlockReplacementState(blockPos, replacementBlock.defaultBlockState());
                                    counter.incrementAndGet();
                                }
                            }
                            case SURROUNDING -> {
                                int areaDimensions = 5; //Check 10 blocks around

                                AABB areaToCheck = AABB.ofSize(blockPos.getCenter(), areaDimensions, areaDimensions, areaDimensions);
                                List<BlockState> surroundingBlocks = chunk.getBlockStates(areaToCheck).filter(blockState -> !blockState.isAir() && !blockState.equals(blockToCheck) && !isRestricted(blockState.getBlock()).getLeft()).toList();
                                CollectionHelper.findObjectWithHighestCount(surroundingBlocks).ifPresentOrElse(blockState -> {
                                    section.setBlockReplacementState(finalBlockPos, blockState);
                                    counter.incrementAndGet();
                                }, () -> LOGGER.error("Couldn't find replacement for Block {} at position {}", blockToCheck.getBlock(), finalBlockPos));
                            }
                        }
                    }
                }
            }
        }

        LOGGER.debug("Found {} block restrictions for chunk {}, took {}ms", counter.get(), chunkPos.toString(), DECIMAL_FORMAT.format((System.nanoTime() - startTime) / 1000000));
        return new LevelChunkExtender(chunkPos, sections, chunk.getHeight(), chunk.getMinBuildHeight());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasBeenCached(ChunkPos chunkPos, BlockPos blockPos) {
        var replacements = this.blockReplacementCache.get(chunkPos.toLong());
        if(replacements != null){
            return replacements.hasReplacement(blockPos);
        }
        else {
            return false;
        }
    }

    @SubscribeEvent
    public void onChunkWatch(final ChunkWatchEvent.Watch event){
        ServerPlayer player = event.getPlayer();

        //ModNetworkHandler.sendToPlayer(new BlockRestrictionsUpdatePacket(this.blockReplacementCache), player);
    }
    @SubscribeEvent
    public void onChunkUnWatch(final ChunkWatchEvent.UnWatch event){
        ServerPlayer player = event.getPlayer();

        //ModNetworkHandler.sendToPlayer(new BlockRestrictionsUpdatePacket(this.blockReplacementCache), player);
    }

    @SubscribeEvent
    public void onChunkSave(final ChunkDataEvent.Save event){
        CompoundTag chunkData = event.getData();
        var replacements = this.blockReplacementCache.get(event.getChunk().getPos().toLong());

        if(replacements != null){
            chunkData.put(MOD_ID + "_block_replacements", replacements.toCompoundTag());
        }
    }

    public static boolean generating = false;

    //TODO: Cache doesn't get cleared on chunk unload
    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event){
        this.blockReplacementCache.remove(event.getChunk().getPos().toLong());
        LOGGER.debug("On unload: {}", this.blockReplacementCache.size());
    }

    @SubscribeEvent
    public void onLevelUpdate(final LevelEvent.UpdateLevelEvent event){
        if(isRunningOnServerThread()){
            PlayerData playerData = event.getPlayer();

            ModNetworkHandler.sendToPlayer(new UpdateRenderersPacket(), playerData.getServerPlayer());
        }
    }


    public void onChunkGeneration(ChunkAccess chunk, ServerLevel level) {
        if(!this.blockReplacementCache.containsKey(chunk.getPos().toLong())){
            var chunkExtender = this.checkChunkForRestrictedBlocks(chunk);
            if(!chunkExtender.isEmpty()){
                this.blockReplacementCache.put(chunk.getPos().toLong(), chunkExtender);
            }
        }
        else {
            LOGGER.warn("Found chunk {} to be newly generated but it already exists in the cache!", chunk.getPos());
        }
        generating = false;
    }

    public BlockState getReplacementIfPresent(BlockState blockStateAtPos, BlockPos blockPos, Player player, Level level){
        if(blockStateAtPos == null) return null;
        blockPos = blockPos.immutable();

        Pair<Boolean, BlockRestriction> pair = this.isRestricted(blockStateAtPos.getBlock());

        if(pair.getLeft()){
            BlockRestriction restriction = pair.getRight();

            if(this.doesNotHaveLevel(player, restriction)){
                LevelChunk chunk = level.getChunkAt(blockPos);
                var chunkExtender = this.getOrCreateChunkExtender(chunk);

                if(chunkExtender != null){
                    BlockState replacementBlockState = chunkExtender.getReplacement(blockPos);;

                    if(replacementBlockState == null) { //TODO: Fix this!
                        //LOGGER.warn("Found that block at setPoint {}, {}, {} is restricted but couldn't find a replacement for it!", blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    }

                    if(replacementBlockState != null){
                        return replacementBlockState;
                    }
                }
                else {
                    LOGGER.error("Couldn't get chunk replacement data for chunk {}", chunk.getPos());
                }
            }
        }

        return blockStateAtPos;
    }


    public LevelChunkExtender getOrCreateChunkExtender(ChunkAccess chunk) {
        LevelChunkExtender chunkExtender = this.blockReplacementCache.get(chunk.getPos().toLong());

        if(chunkExtender == null){
            LOGGER.warn("Couldn't find requested chunk replacement data for chunk {}, trying to recreate it!", chunk.getPos());
            chunkExtender = this.checkChunkForRestrictedBlocks(chunk);
            this.blockReplacementCache.put(chunk.getPos().toLong(), chunkExtender);
        }

        return chunkExtender;
    }


    public static class LevelChunkExtender implements LevelHeightAccessor {
        private final int height;
        private final int minBuildHeight;
        private final ChunkPos chunkPos;
        private final Long2ObjectOpenHashMap<LevelChunkSectionExtender> sections;
        private final Long2LongOpenHashMap keyMap;


        public LevelChunkExtender(ChunkPos chunkPos, int expectedAmountOfSections, int minBuildHeight, int height) {
            this(chunkPos, new Long2ObjectOpenHashMap<>(expectedAmountOfSections), height, minBuildHeight);
        }
        public LevelChunkExtender(ChunkPos chunkPos, Long2ObjectOpenHashMap<LevelChunkSectionExtender> sections, int height, int minBuildHeight) {
            this.chunkPos = chunkPos;
            this.sections = sections;
            this.height = height;
            this.minBuildHeight = minBuildHeight;

            this.keyMap = new Long2LongOpenHashMap(this.sections.size());
            for(LevelChunkSectionExtender section : this.sections.values()){
                int bottomBlockY = section.bottomBlockY;
                int topBlockY = bottomBlockY + LevelChunkSection.SECTION_HEIGHT;

                this.keyMap.put(bottomBlockY, topBlockY);
            }
        }


        public static LevelChunkExtender fromCompoundTag(CompoundTag tag) {
            ChunkPos setPoint = new ChunkPos(tag.getInt("xPos"), tag.getInt("zPos"));
            int height = tag.getInt("height");
            int minBuildHeight = tag.getInt("minBuildHeight");
            ListTag tags = tag.getList("sections", 10);
            Long2ObjectOpenHashMap<LevelChunkSectionExtender> sections = new Long2ObjectOpenHashMap<>(tags.size());
            tags.forEach(tag1 -> {
                sections.put(((CompoundTag) tag1).getInt("bottomBlockY"), LevelChunkSectionExtender.fromCompoundTag((CompoundTag) tag1));
            });

            return new LevelChunkExtender(setPoint, sections, height, minBuildHeight);
        }


        public boolean hasReplacement(BlockPos blockPos){
            LevelChunkSectionExtender section = this.getSection(blockPos);

            if(section != null){
                return section.hasReplacement(blockPos);
            }

            return false;
        }

        @Nullable
        public BlockState getReplacement(BlockPos blockPos){
            LevelChunkSectionExtender section = this.getSection(blockPos);

            if(section != null){
                BlockState replacement = section.getBlockReplacementState(blockPos);
                if(replacement != null && !replacement.equals(defaultReplacement)){
                    return replacement;
                }
            }

            return null;
        }

        public boolean isEmpty(){
            return this.sections.isEmpty();
        }

        @Nullable
        public LevelChunkSectionExtender getSection(BlockPos blockPos){
            int y = blockPos.getY();
            for(Long2LongMap.Entry entry : this.keyMap.long2LongEntrySet()) {
                long bottom = entry.getLongKey();
                long top = entry.getLongValue();

                if(bottom <= y && y <= top){
                    return this.sections.get(bottom);
                }
            }

            return null;
        }


        public ChunkPos getChunkPos() {
            return chunkPos;
        }


        @Override
        public int getHeight() {
            return this.height;
        }


        @Override
        public int getMinBuildHeight() {
            return this.minBuildHeight;
        }


        public CompoundTag toCompoundTag() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("xPos", this.chunkPos.x);
            tag.putInt("zPos", this.chunkPos.z);
            tag.putInt("height", this.height);
            tag.putInt("minBuildHeight", this.getMinBuildHeight());


            ListTag listTag = new ListTag();
            this.sections.forEach((aLong, levelChunkSectionExtender) -> {
                listTag.add(levelChunkSectionExtender.toCompoundTag());
            });
            tag.put("sections", listTag);
            return tag;
        }
    }

    public static class LevelChunkSectionExtender{
        private final int bottomBlockY;
        private short nonEmptyBlockCount;
        private final PalettedContainer<BlockState> states;
        public LevelChunkSectionExtender(int bottomBlockY, PalettedContainer<BlockState> pStates) {
            this.bottomBlockY = bottomBlockY;
            this.states = pStates;
            this.recalcBlockCounts();
        }

        public LevelChunkSectionExtender(int bottomBlockY) {
            this.bottomBlockY = bottomBlockY;
            this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, defaultReplacement, PalettedContainer.Strategy.SECTION_STATES);
        }

        public static int getBottomBlockY(int sectionY) {
            return sectionY << 4;
        }

        public BlockState getBlockReplacementState(BlockPos blockPos) {
            return this.states.get(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);
        }

        public FluidState getFluidReplacementState(BlockPos blockPos) {
            return this.states.get(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15).getFluidState();
        }

        public void acquire() {
            this.states.acquire();
        }

        public void release() {
            this.states.release();
        }

        public BlockState setBlockReplacementState(BlockPos blockPos, BlockState pState) {
            return this.setBlockReplacementState(blockPos, pState, true);
        }

        public BlockState setBlockReplacementState(BlockPos blockPos, BlockState pState, boolean pUseLocks) {
            int pX = blockPos.getX() & 15, pY = blockPos.getY() & 15, pZ = blockPos.getZ() & 15;
            BlockState blockstate;
            if (pUseLocks) {
                blockstate = this.states.getAndSet(pX, pY, pZ, pState);
            }
            else {
                blockstate = this.states.getAndSetUnchecked(pX, pY, pZ, pState);
            }

            if (!blockstate.isAir()) {
                --this.nonEmptyBlockCount;
            }

            if (!pState.isAir()) {
                ++this.nonEmptyBlockCount;
            }

            return blockstate;
        }

        */
/**
 * @return {@code true} if this section consists only of air-like blocks.
 *//*

        public boolean hasOnlyAir() {
            return this.nonEmptyBlockCount == 0;
        }

        */
/**
 * @return The lowest y coordinate in this section.
 *//*

        public int bottomBlockY() {
            return this.bottomBlockY;
        }

        public void recalcBlockCounts() {
            class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
                public int nonEmptyBlockCount;

                public void accept(BlockState blockState, int count) {
                    FluidState fluidstate = blockState.getFluidState();
                    if (!blockState.isAir()) {
                        this.nonEmptyBlockCount += count;
                    }

                    if (!fluidstate.isEmpty()) {
                        this.nonEmptyBlockCount += count;
                    }

                }
            }

            BlockCounter levelchunksection$1blockcounter = new BlockCounter();
            this.states.count(levelchunksection$1blockcounter);
            this.nonEmptyBlockCount = (short)levelchunksection$1blockcounter.nonEmptyBlockCount;
        }

        public PalettedContainer<BlockState> getReplacementStates() {
            return this.states;
        }

        public void read(FriendlyByteBuf pBuffer) {
            this.nonEmptyBlockCount = pBuffer.readShort();
            this.states.read(pBuffer);
        }

        public void write(FriendlyByteBuf pBuffer) {
            pBuffer.writeShort(this.nonEmptyBlockCount);
            this.states.write(pBuffer);
        }

        public int getSerializedSize() {
            return 2 + this.states.getSerializedSize();
        }

        */
/**
 * @return {@code true} if this section has any states matching the given predicate. As the internal representation
 * uses a {@link net.minecraft.world.level.chunk.Palette}, this is more efficient than looping through every position
 * in the section, or indeed the chunk.
 *//*

        public boolean maybeHas(Predicate<BlockState> pPredicate) {
            return this.states.maybeHas(pPredicate);
        }


        public boolean hasReplacement(BlockPos blockPos) {
            return this.getBlockReplacementState(blockPos) != defaultReplacement;
        }


        public CompoundTag toCompoundTag() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("bottomBlockY", this.bottomBlockY);
            tag.put("block_replacements", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, this.states).getOrThrow(false, LOGGER::error));
            return tag;
        }

        public static LevelChunkSectionExtender fromCompoundTag(CompoundTag tag){
            int bottomBlockY = tag.getInt("bottomBlockY");
            PalettedContainer<BlockState> block_replacements = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, tag.getCompound("block_replacements")).getOrThrow(false, LOGGER::error);

            return new LevelChunkSectionExtender(bottomBlockY, block_replacements);
        }
    }
}
*/
