package de.thedead2.progression_reloaded.player.types;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.client.display.TeamDisplayInfo;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.Consumer;


public class PlayerTeam {

    private final String teamName;

    private final ResourceLocation id;

    private final Set<SinglePlayer> activeMembers = new HashSet<>();

    private final Set<KnownPlayer> knownMembers = new HashSet<>();

    private final Map<ResourceLocation, IAbility<?>> teamAbilities = new HashMap<>();

    private ProgressionLevel progressionLevel;


    public PlayerTeam(String teamName, ResourceLocation id, Collection<KnownPlayer> knownMembers) {
        this(teamName, id, knownMembers, null);
    }


    public PlayerTeam(String teamName, ResourceLocation id, Collection<KnownPlayer> knownMembers, ProgressionLevel level) {
        this.teamName = teamName;
        this.id = id;
        this.knownMembers.addAll(knownMembers);
        this.progressionLevel = level;
    }


    //TODO: Data doesn't get saved --> members and level
    public static PlayerTeam fromCompoundTag(CompoundTag tag) {
        if(tag == null || tag.isEmpty()) {
            return null;
        }
        String level = tag.getString("level");
        String name = tag.getString("name");
        String id = tag.getString("id");
        CompoundTag members = tag.getCompound("members");
        List<KnownPlayer> memberIds = new ArrayList<>();
        members.getAllKeys().forEach(s -> memberIds.add(new KnownPlayer(ResourceLocation.tryParse(s), members.getString(s))));

        return new PlayerTeam(name, new ResourceLocation(id), memberIds, ProgressionLevel.fromKey(new ResourceLocation(level)));
    }


    public static PlayerTeam fromRegistry(String teamName, ServerPlayer player) {
        var team = fromKey(ResourceLocation.tryParse(teamName));
        if(team != null && team.accept(player)) {
            return team;
        }
        else {
            return null;
        }
    }


    public static PlayerTeam fromKey(ResourceLocation teamId) {
        return PlayerDataHandler.getTeam(teamId);
    }


    private boolean accept(Player player) {
        return this.isPlayerInTeam(KnownPlayer.fromPlayer(player));
    }


    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name.toLowerCase().replaceAll(" ", "_"));
    }


    public void updateProgressionLevel(ProgressionLevel level, SinglePlayer player) {
        this.progressionLevel = level;
        this.activeMembers.forEach(player1 -> {
            if(!player1.equals(player)) {
                player1.updateProgressionLevel(level);
            }
        });
    }


    public ImmutableSet<SinglePlayer> getActiveMembers() {
        return ImmutableSet.copyOf(activeMembers);
    }


    public String getName() {
        return teamName;
    }


    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        if(this.progressionLevel != null) {
            tag.putString("level", this.progressionLevel.getId().toString());
        }
        tag.putString("name", this.teamName);
        tag.putString("id", this.id.toString());
        CompoundTag members = new CompoundTag();
        this.knownMembers.forEach(knownPlayer -> members.putString(knownPlayer.id().toString(), knownPlayer.name()));
        tag.put("members", members);
        return tag;
    }


    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }


    public ResourceLocation getId() {
        return this.id;
    }


    public void addActivePlayer(SinglePlayer singlePlayer) {
        if(singlePlayer == null || !this.accept(singlePlayer)) {
            return;
        }
        activeMembers.add(singlePlayer);
        singlePlayer.setTeam(this);
    }


    public void addPlayers(Collection<KnownPlayer> players) {
        players.forEach(this::addPlayer);
    }


    private void addPlayer(KnownPlayer knownPlayer) {
        this.knownMembers.add(knownPlayer);
        this.addActivePlayer(PlayerDataHandler.getActivePlayer(knownPlayer));
    }


    private boolean accept(SinglePlayer player) {
        return this.isPlayerInTeam(KnownPlayer.fromSinglePlayer(player));
    }


    public boolean isPlayerInTeam(KnownPlayer player) {
        return knownMembers.contains(player);
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


    public void removeActivePlayer(SinglePlayer singlePlayer) {
        activeMembers.remove(singlePlayer);
    }


    public ImmutableCollection<KnownPlayer> getMembers() {
        return ImmutableSet.copyOf(this.knownMembers);
    }


    public void addAbilities(Collection<IAbility<?>> abilities) {
        abilities.forEach(this::addAbility);
    }


    public void addAbility(IAbility<?> ability) {
        this.teamAbilities.put(ability.getId(), ability);
        this.activeMembers.forEach(player -> player.addAbility(ability));
    }


    public void removeAbilities(Collection<IAbility<?>> abilities) {
        abilities.forEach(this::removeAbility);
    }


    public void removeAbility(IAbility<?> ability) {
        this.teamAbilities.remove(ability.getId());
        this.activeMembers.forEach(player -> player.removeAbility(ability));
    }


    public boolean hasAbility(IAbility<?> ability) {
        return this.teamAbilities.containsValue(ability);
    }


    public void forEachMember(Consumer<KnownPlayer> action) {
        this.knownMembers.forEach(action);
    }


    public TeamDisplayInfo getDisplay() {
        return new TeamDisplayInfo(this.teamName, this.knownMembers);
    }
}
