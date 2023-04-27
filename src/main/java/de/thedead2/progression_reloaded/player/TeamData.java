package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TeamData extends SavedData {
    private final Map<ResourceLocation, PlayerTeam> teams = new HashMap<>();

    public TeamData(Map<ResourceLocation, PlayerTeam> teams){
        this.teams.putAll(teams);
        this.setDirty();
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        this.teams.forEach((id, playerTeam) -> tag.put(id.toString(), playerTeam.toCompoundTag()));
        return tag;
    }

    public static TeamData load(CompoundTag tag) {
        Map<ResourceLocation, PlayerTeam> teamMap = new HashMap<>();
        tag.getAllKeys().forEach(s -> teamMap.put(ResourceLocation.tryParse(s), PlayerTeam.fromCompoundTag(tag.getCompound(s))));
        return new TeamData(teamMap);
    }

    public void addTeam(PlayerTeam team){
        this.teams.put(team.getId(), team);
        this.setDirty();
    }

    public PlayerTeam getTeam(ResourceLocation id){
        return this.teams.get(id);
    }

    public Collection<PlayerTeam> allTeams() {
        return this.teams.values();
    }
}
