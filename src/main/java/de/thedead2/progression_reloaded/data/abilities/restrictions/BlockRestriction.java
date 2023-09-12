package de.thedead2.progression_reloaded.data.abilities.restrictions;

import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BlockRestriction extends Restriction<Block> {


    public BlockRestriction(@NotNull ResourceLocation levelId, RestrictionKey<Block> key) {
        super(levelId, key);
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {

    }
}
