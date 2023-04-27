package de.thedead2.progression_reloaded.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerTeam {

    private final String teamName;
    private final ResourceLocation id;
    private ProgressionLevel progressionLevel;
    private final Set<PlayerData> members = new HashSet<>();

    public PlayerTeam(String teamName, ResourceLocation id, ProgressionLevel progressionLevel) {
        this.teamName = teamName;
        this.id = id;
        this.progressionLevel = progressionLevel;
    }

    public static PlayerTeam fromCompoundTag(CompoundTag tag) {
        if(tag == null || tag.isEmpty()) return null;
        String level = tag.getString("level");
        String name = tag.getString("name");
        String id = tag.getString("id");
        return new PlayerTeam(name, ResourceLocation.tryParse(id), ProgressionLevel.fromKey(ResourceLocation.tryParse(level)));
    }

    public static PlayerTeam fromRegistry(ResourceLocation teamId) {
        return ModRegistries.PROGRESSION_TEAM_DATA.get().getValue(teamId);
    }

    public void addPlayer(PlayerData player){
        members.add(player);
    }

    public void removePlayer(PlayerData player){
        members.remove(player);
    }

    public boolean isPlayerInTeam(PlayerData player){
        return members.contains(player);
    }

    public Set<PlayerData> getMembers() {
        return members;
    }

    public String getTeamName() {
        return teamName;
    }

    public void updateProgressionLevel(ProgressionLevel level){
        this.progressionLevel = level;
        this.members.forEach(player -> player.updateProgressionLevel(level));
    }

    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("level", this.progressionLevel.getId().toString());
        tag.putString("name", this.teamName);
        tag.putString("id", this.id.toString());
        return tag;
    }
}
