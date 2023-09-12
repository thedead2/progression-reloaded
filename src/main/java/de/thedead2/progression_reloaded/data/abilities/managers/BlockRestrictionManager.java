package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.restrictions.BlockRestriction;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class BlockRestrictionManager extends RestrictionManager<BlockRestriction, Block> {

    public BlockRestrictionManager() {
        super(new ResourceLocation(ModHelper.MOD_ID, "block_restriction_manager"), () -> DefaultAction.DENY);
    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return null;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }


    @Override
    public @NotNull String getName() {
        return "BlockRestrictionManager";
    }


    @Override
    public ImmutablePair<Boolean, BlockRestriction> isRestricted(Block block) {
        return null;
    }
}
