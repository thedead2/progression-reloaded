package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.restrictions.RecipeRestriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.function.Supplier;


public class RecipeRestrictionManager extends RestrictionManager<RecipeRestriction, Recipe<?>> {


    protected RecipeRestrictionManager(ResourceLocation id, Supplier<DefaultAction> defaultAction) {
        super(id, defaultAction);
    }


    @Override
    public ImmutablePair<Boolean, RecipeRestriction> isRestricted(Recipe<?> object) {
        return null;
    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return null;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }


    @Override
    public String getName() {
        return super.getName();
    }
}
