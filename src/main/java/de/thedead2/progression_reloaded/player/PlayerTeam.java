package de.thedead2.progression_reloaded.player;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PlayerTeam {
    private final String teamName;
    private final ResourceLocation id;
    private ProgressionLevel progressionLevel;
    private final Set<SinglePlayer> activeMembers = new HashSet<>();
    private final Set<KnownPlayer> knownMembers = new HashSet<>();

    public PlayerTeam(String teamName, ResourceLocation id, Collection<KnownPlayer> knownMembers) {
        this.teamName = teamName;
        this.id = id;
        this.knownMembers.addAll(knownMembers);
    }

    public static PlayerTeam fromCompoundTag(CompoundTag tag) {
        if(tag == null || tag.isEmpty()) return null;
        String level = tag.getString("level");
        String name = tag.getString("name");
        String id = tag.getString("id");
        CompoundTag members = tag.getCompound("members");
        Set<KnownPlayer> memberIds = new HashSet<>();
        members.getAllKeys().forEach(s -> memberIds.add(new KnownPlayer(ResourceLocation.tryParse(s), members.getString(s))));
        return new PlayerTeam(name, ResourceLocation.tryParse(id), memberIds);
    }

    public static PlayerTeam fromRegistry(String teamName, ServerPlayer player) {
        var team = PlayerDataHandler.getTeamData().orElseThrow().getTeam(ResourceLocation.tryParse(teamName));
        if(team != null && team.accept(player)) return team;
        else return null;
    }

    private boolean accept(Player player) {
        return this.isPlayerInTeam(KnownPlayer.fromPlayer(player));
    }

    private boolean accept(SinglePlayer player) {
        return this.isPlayerInTeam(KnownPlayer.fromSinglePlayer(player));
    }

    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name.toLowerCase().replaceAll(" ", "_"));
    }

    public void addActivePlayer(SinglePlayer singlePlayer){
        if (singlePlayer == null || !this.accept(singlePlayer)) return;
        activeMembers.add(singlePlayer);
        singlePlayer.setTeam(this);
    }

    public void removeActivePlayer(SinglePlayer singlePlayer){
        activeMembers.remove(singlePlayer);
    }

    public boolean isPlayerInTeam(KnownPlayer player){
        return knownMembers.contains(player);
    }

    public ImmutableSet<SinglePlayer> getActiveMembers() {
        return ImmutableSet.copyOf(activeMembers);
    }

    public String getName() {
        return teamName;
    }

    public void updateProgressionLevel(ProgressionLevel level, SinglePlayer player){
        this.progressionLevel = level;
        this.activeMembers.forEach(player1 -> {
            if(!player1.equals(player)) player1.updateProgressionLevel(level);
        });
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
        this.knownMembers.forEach(knownPlayer -> members.putString(knownPlayer.id().toString(), knownPlayer.name()));
        tag.put("members", members);
        return tag;
    }

    public void addPlayers(Collection<KnownPlayer> players) {
        players.forEach(this::addPlayer);
    }

    private void addPlayer(KnownPlayer knownPlayer) {
        this.knownMembers.add(knownPlayer);
        this.addActivePlayer(PlayerDataHandler.getPlayerData().orElseThrow().getActivePlayer(knownPlayer));
    }

    public void removePlayers(Collection<KnownPlayer> players) {
        players.forEach(this::removePlayer);
    }

    private void removePlayer(KnownPlayer knownPlayer) {
        this.knownMembers.remove(knownPlayer);
        var singlePlayer = PlayerDataHandler.getPlayerData().orElseThrow().getActivePlayer(knownPlayer);
        this.removeActivePlayer(singlePlayer);
        singlePlayer.setTeam(null);
    }

    public ImmutableCollection<KnownPlayer> getMembers() {
        return ImmutableSet.copyOf(this.knownMembers);
    }
}
