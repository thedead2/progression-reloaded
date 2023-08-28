package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class TeamData extends SavedData {

    private final Map<ResourceLocation, PlayerTeam> teams = new HashMap<>();


    public TeamData(Map<ResourceLocation, PlayerTeam> teams) {
        this.teams.putAll(teams);
        this.setDirty();
    }


    public static TeamData load(CompoundTag tag) {
        Map<ResourceLocation, PlayerTeam> teamMap = new HashMap<>();
        tag.getAllKeys().forEach(s -> teamMap.put(
                ResourceLocation.tryParse(s),
                PlayerTeam.fromCompoundTag(tag.getCompound(s))
        ));
        return new TeamData(teamMap);
    }


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        this.teams.forEach((id, playerTeam) -> tag.put(id.toString(), playerTeam.toCompoundTag()));
        return tag;
    }


    public void addTeam(PlayerTeam team) {
        this.teams.put(team.getId(), team);
        this.setDirty();
    }


    public PlayerTeam getTeam(KnownPlayer player) {
        return this.teams.values().stream().filter(playerTeam -> playerTeam.isPlayerInTeam(player)).findAny().orElse(
                null);
    }


    public PlayerTeam getTeam(String teamName) {
        return getTeam(PlayerTeam.createId(teamName));
    }


    public PlayerTeam getTeam(ResourceLocation id) {
        return this.teams.get(id);
    }


    public ImmutableCollection<PlayerTeam> allTeams() {
        return ImmutableSet.copyOf(this.teams.values());
    }


    public boolean removeTeam(PlayerTeam team) {
        return removeTeam(team.getId());
    }


    public boolean removeTeam(ResourceLocation id) {
        this.setDirty();
        PlayerTeam team = this.teams.get(id);
        if(team != null) {
            team.getActiveMembers().forEach(singlePlayer -> singlePlayer.setTeam(null));
        }
        this.teams.remove(id);
        return team != null;
    }
}
