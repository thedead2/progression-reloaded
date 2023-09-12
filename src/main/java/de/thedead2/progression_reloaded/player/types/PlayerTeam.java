package de.thedead2.progression_reloaded.player.types;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.display.TeamDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.Consumer;


public class PlayerTeam {

    private final String teamName;

    private final ResourceLocation id;

    private final Set<PlayerData> activeMembers = new HashSet<>();

    private final Set<KnownPlayer> knownMembers = new HashSet<>();

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
        members.getAllKeys().forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(members.getCompound(s));
            if(!PlayerDataHandler.playerLastOnlineLongAgo(player)) {
                memberIds.add(player);
            }
        });

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


    public static PlayerTeam fromNetwork(FriendlyByteBuf buf) {
        String teamName = buf.readUtf();
        ResourceLocation id = buf.readResourceLocation();
        Set<KnownPlayer> members = buf.readCollection(Sets::newHashSetWithExpectedSize, KnownPlayer::fromNetwork);
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());

        return new PlayerTeam(teamName, id, members, level);
    }


    public void updateProgressionLevel(ProgressionLevel level, PlayerData player) {
        this.progressionLevel = level;
        this.activeMembers.forEach(player1 -> {
            if(!player1.equals(player)) {
                player1.updateProgressionLevel(level);
            }
        });
    }


    public String getName() {
        return teamName;
    }


    public ImmutableSet<PlayerData> getActiveMembers() {
        return ImmutableSet.copyOf(activeMembers);
    }


    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }


    public ResourceLocation getId() {
        return this.id;
    }


    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        if(this.progressionLevel != null) {
            tag.putString("level", this.progressionLevel.getId().toString());
        }
        tag.putString("name", this.teamName);
        tag.putString("id", this.id.toString());
        CompoundTag members = new CompoundTag();
        this.knownMembers.forEach(knownPlayer -> members.put(knownPlayer.id().toString(), knownPlayer.toCompoundTag()));
        tag.put("members", members);
        return tag;
    }


    public void addPlayers(Collection<KnownPlayer> players) {
        players.forEach(this::addPlayer);
    }


    private void addPlayer(KnownPlayer knownPlayer) {
        this.knownMembers.add(knownPlayer);
        this.addActivePlayer(PlayerDataHandler.getActivePlayer(knownPlayer));
    }


    public void addActivePlayer(PlayerData playerData) {
        if(playerData == null || !this.accept(playerData)) {
            return;
        }
        activeMembers.add(playerData);
        playerData.setTeam(this);
    }


    public boolean isPlayerInTeam(KnownPlayer player) {
        return knownMembers.contains(player);
    }


    public void removePlayers(Collection<KnownPlayer> players) {
        players.forEach(this::removePlayer);
    }


    private boolean accept(PlayerData player) {
        return this.isPlayerInTeam(KnownPlayer.fromSinglePlayer(player));
    }


    private void removePlayer(KnownPlayer knownPlayer) {
        this.knownMembers.remove(knownPlayer);
        var singlePlayer = PlayerDataHandler.getActivePlayer(knownPlayer);
        this.removeActivePlayer(singlePlayer);
        singlePlayer.setTeam(null);
    }


    public ImmutableCollection<KnownPlayer> getMembers() {
        return ImmutableSet.copyOf(this.knownMembers);
    }


    public void forEachMember(Consumer<KnownPlayer> action) {
        this.knownMembers.forEach(action);
    }


    public TeamDisplayInfo getDisplay() {
        return new TeamDisplayInfo(this.teamName, this.knownMembers);
    }


    public void removeActivePlayer(PlayerData playerData) {
        activeMembers.remove(playerData);
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.teamName);
        buf.writeResourceLocation(this.id);
        buf.writeCollection(this.knownMembers, (buf1, player) -> player.toNetwork(buf1));
        buf.writeResourceLocation(this.progressionLevel.getId());
    }
}
