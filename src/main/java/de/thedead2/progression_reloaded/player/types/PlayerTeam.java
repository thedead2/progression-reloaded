package de.thedead2.progression_reloaded.player.types;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.display.TeamDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
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

    private final Map<KnownPlayer, PlayerQuests> pendingQuestSyncs = new HashMap<>();

    private ProgressionLevel progressionLevel;


    public PlayerTeam(String teamName, ResourceLocation id, Collection<KnownPlayer> knownMembers) {
        this(teamName, id, knownMembers, new HashMap<>(), null);
    }


    public PlayerTeam(String teamName, ResourceLocation id, Collection<KnownPlayer> knownMembers, Map<KnownPlayer, PlayerQuests> pendingQuestSyncs, ProgressionLevel level) {
        this.teamName = teamName;
        this.id = id;
        this.knownMembers.addAll(knownMembers);
        this.progressionLevel = level;

        this.pendingQuestSyncs.putAll(pendingQuestSyncs);
    }


    public static PlayerTeam fromCompoundTag(CompoundTag tag) {
        if(tag == null || tag.isEmpty()) {
            return null;
        }
        String level = tag.getString("level");
        String name = tag.getString("name");
        String id = tag.getString("id");
        Set<KnownPlayer> members = CollectionHelper.loadFromNBT(Sets::newHashSetWithExpectedSize, tag.getList("members", 0), tag1 -> KnownPlayer.fromCompoundTag((CompoundTag) tag1));
        Map<KnownPlayer, PlayerQuests> pendingQuestSyncs = CollectionHelper.loadFromNBT(tag.getCompound("pendingQuestSyncs"), KnownPlayer::fromString, tag1 -> PlayerQuests.loadFromNBT((CompoundTag) tag1));

        return new PlayerTeam(name, new ResourceLocation(id), members, pendingQuestSyncs, ProgressionLevel.fromKey(new ResourceLocation(level)));
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
        return PlayerDataManager.getTeam(teamId);
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
        Map<KnownPlayer, PlayerQuests> pendingQuestSyncs = buf.readMap(Maps::newHashMapWithExpectedSize, KnownPlayer::fromNetwork, PlayerQuests::loadFromNetwork);
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());

        return new PlayerTeam(teamName, id, members, pendingQuestSyncs, level);
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
        tag.put("members", CollectionHelper.saveToNBT(this.knownMembers, KnownPlayer::toCompoundTag));
        tag.put("pendingQuestSyncs", CollectionHelper.saveToNBT(this.pendingQuestSyncs, KnownPlayer::toString, PlayerQuests::saveToNBT));

        return tag;
    }


    public void addPlayers(Collection<KnownPlayer> players) {
        players.forEach(this::addPlayer);
    }


    private void addPlayer(KnownPlayer knownPlayer) {
        this.knownMembers.add(knownPlayer);
        this.addActivePlayer(PlayerDataManager.getPlayerData(knownPlayer));
    }


    public void addActivePlayer(PlayerData playerData) {
        if(playerData == null || !this.accept(playerData)) {
            return;
        }
        this.activeMembers.add(playerData);
        playerData.setTeam(this);

        KnownPlayer key = KnownPlayer.fromSinglePlayer(playerData);
        PlayerQuests playerQuests = this.pendingQuestSyncs.get(key);

        if(playerQuests != null) {
            playerData.copyQuestProgress(playerQuests);
            this.pendingQuestSyncs.remove(key);
        }
    }


    public boolean isPlayerInTeam(KnownPlayer player) {
        return this.knownMembers.contains(player);
    }


    public void removePlayers(Collection<KnownPlayer> players) {
        players.forEach(this::removePlayer);
    }


    private boolean accept(PlayerData player) {
        return this.isPlayerInTeam(KnownPlayer.fromSinglePlayer(player));
    }


    private void removePlayer(KnownPlayer knownPlayer) {
        this.knownMembers.remove(knownPlayer);
        var singlePlayer = PlayerDataManager.getPlayerData(knownPlayer);
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
        buf.writeMap(this.pendingQuestSyncs, (friendlyByteBuf, player) -> player.toNetwork(friendlyByteBuf), (friendlyByteBuf, playerQuests) -> playerQuests.saveToNetwork(friendlyByteBuf));
        buf.writeResourceLocation(this.progressionLevel.getId());
    }


    public void queueQuestSync(KnownPlayer member, PlayerQuests playerQuests) {
        this.pendingQuestSyncs.put(member, playerQuests);
    }
}
