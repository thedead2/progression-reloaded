package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.restrictions.DimensionRestriction;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class DimensionRestrictionManager extends RestrictionManager<DimensionRestriction, ResourceKey<Level>> {


    public DimensionRestrictionManager() {
        super(new ResourceLocation(ModHelper.MOD_ID, "dimension_restriction_manager"), () -> DefaultAction.DENY);
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
        return "DimensionRestrictionManager";
    }


    @SubscribeEvent
    public void onDimensionChange(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Pair<Boolean, DimensionRestriction> restrictionPair = isRestricted(event.getDimension());
        if(entity instanceof ServerPlayer player && restrictionPair.getLeft()) {
            DimensionRestriction restriction = restrictionPair.getRight();
            if(this.doesNotHaveLevel(player, restriction)) {
                event.setCanceled(true);
                if(restriction.getRestrictionMessage() != null) {
                    player.sendSystemMessage(restriction.getRestrictionMessage());
                }
            }
        }
    }


    @Override
    public ImmutablePair<Boolean, DimensionRestriction> isRestricted(ResourceKey<Level> dimensionResourceKey) {
        ResourceLocation dimensionId = dimensionResourceKey.location();
        return isRestrictedById(dimensionId);
    }
}
