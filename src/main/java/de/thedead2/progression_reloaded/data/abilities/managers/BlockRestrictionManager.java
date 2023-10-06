package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.data.abilities.restrictions.BlockRestriction;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;


public class BlockRestrictionManager extends RestrictionManager<BlockRestriction, Block> {


    public BlockRestrictionManager() {
        super(new ResourceLocation(ModHelper.MOD_ID, "block_restriction_manager"), () -> DefaultAction.DENY);

        this.addRestriction(ForgeRegistries.BLOCKS.getKey(Blocks.GRASS_BLOCK), new BlockRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(ForgeRegistries.BLOCKS.getKey(Blocks.GRASS_BLOCK)), BlockRestriction.ReplacementMode.SURROUNDING, null));
        this.addRestriction(BlockTags.COAL_ORES, new BlockRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(BlockTags.COAL_ORES), BlockRestriction.ReplacementMode.SURROUNDING, null));
        this.addRestriction(ForgeRegistries.BLOCKS.getKey(Blocks.WATER), new BlockRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(ForgeRegistries.BLOCKS.getKey(Blocks.WATER)), BlockRestriction.ReplacementMode.STATIC,
                                                                                              ForgeRegistries.BLOCKS.getKey(Blocks.LAVA)
        ));

    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return null;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }


    @SubscribeEvent
    public void onBlockBreak(final BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        Player player = event.getPlayer();
        BlockState blockToBreak = event.getState();
        BlockPos blockPos = event.getPos();

        Pair<Boolean, BlockRestriction> pair = this.isRestricted(blockToBreak.getBlock());

        if(pair.getLeft()) {
            BlockRestriction restriction = pair.getRight();

        }
    }


    @Override
    public ImmutablePair<Boolean, BlockRestriction> isRestricted(Block block) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        var pair1 = isRestrictedById(blockId);
        return pair1.getLeft() ? pair1 : isRestrictedByTag(block.builtInRegistryHolder().getTagKeys());
    }
}
