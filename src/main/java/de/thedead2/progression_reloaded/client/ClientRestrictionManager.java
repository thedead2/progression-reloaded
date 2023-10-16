package de.thedead2.progression_reloaded.client;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.managers.RestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.restrictions.Restriction;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;


public class ClientRestrictionManager extends RestrictionManager<Restriction<?>, Object> {

    protected ClientRestrictionManager() {
        super(new ResourceLocation(ModHelper.MOD_ID, "client_restriction_manager"), () -> DefaultAction.DENY);
    }


    @Override
    public ImmutablePair<Boolean, Restriction<?>> isRestricted(Object object) {
        return null;
    }


    @Override
    public @NotNull String getName() {
        return "ClientRestrictionManager";
    }


    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        return Collections.emptyMap();
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {}
}
