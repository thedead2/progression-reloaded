package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.data.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerTeam {

    private final String teamName;
    private final ResourceLocation id;
    private ProgressionLevel progressionLevel;
    private final Set<SinglePlayer> activeMembers = new HashSet<>();
    private final Set<ResourceLocation> knownMembers = new HashSet<>();

    public PlayerTeam(String teamName, ResourceLocation id, Collection<ResourceLocation> knownMembers, ProgressionLevel progressionLevel) {
        this.teamName = teamName;
        this.id = id;
        this.progressionLevel = progressionLevel;
        this.knownMembers.addAll(knownMembers);
    }

    public static PlayerTeam fromCompoundTag(CompoundTag tag) {
        if(tag == null || tag.isEmpty()) return null;
        String level = tag.getString("level");
        String name = tag.getString("name");
        String id = tag.getString("id");
        CompoundTag members = tag.getCompound("members");
        Set<ResourceLocation> memberIds = new HashSet<>();
        members.getAllKeys().forEach(s -> memberIds.add(ResourceLocation.tryParse(s)));
        return new PlayerTeam(name, ResourceLocation.tryParse(id), memberIds, ProgressionLevel.fromKey(ResourceLocation.tryParse(level)));
    }

    public static PlayerTeam fromRegistry(ResourceLocation teamId) {
        return PlayerDataHandler.getTeamData().orElseThrow().getTeam(teamId);
    }

    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name.toLowerCase().replaceAll(" ", "_"));
    }

    public void addActivePlayer(SinglePlayer singlePlayer){
        activeMembers.add(singlePlayer);
        knownMembers.add(singlePlayer.getId());
    }

    public void removePlayer(SinglePlayer singlePlayer){
        activeMembers.remove(singlePlayer);
    }

    public boolean isPlayerInTeam(SinglePlayer singlePlayer){
        return activeMembers.contains(singlePlayer);
    }

    public Set<SinglePlayer> getActiveMembers() {
        return activeMembers;
    }

    public String getTeamName() {
        return teamName;
    }

    public void updateProgressionLevel(ProgressionLevel level){
        this.progressionLevel = level;
        this.activeMembers.forEach(player -> player.updateProgressionLevel(level));
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
        CompoundTag members = new CompoundTag();
        AtomicInteger i = new AtomicInteger();
        this.knownMembers.forEach(id -> members.putInt(id.toString(), i.getAndIncrement()));
        tag.put("members", members);
        return tag;
    }

    public void addPlayers(Collection<SinglePlayer> players) {
        players.forEach(this::addActivePlayer);
    }
}
