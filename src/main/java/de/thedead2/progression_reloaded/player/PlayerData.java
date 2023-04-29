package de.thedead2.progression_reloaded.player;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class PlayerData extends SavedData {
    private final Set<KnownPlayer> knownPlayers = new HashSet<>();
    private final Map<ResourceLocation, SinglePlayer> activePlayers = new HashMap<>();

    public PlayerData(Collection<KnownPlayer> knownPlayers){
        this.knownPlayers.addAll(knownPlayers);
        this.setDirty();
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        knownPlayers.forEach(knownPlayer -> tag.putString(knownPlayer.id().toString(), knownPlayer.name()));
        return tag;
    }

    public static PlayerData load(CompoundTag tag) {
        Set<KnownPlayer> knownPlayers = new HashSet<>();
        tag.getAllKeys().forEach(s -> knownPlayers.add(new KnownPlayer(ResourceLocation.tryParse(s), tag.getString(s))));
        return new PlayerData(knownPlayers);
    }

    public void addActivePlayer(SinglePlayer singlePlayer){
        this.activePlayers.put(singlePlayer.getId(), singlePlayer);
        this.addKnownPlayer(singlePlayer.getPlayer());
        singlePlayer.getTeam().ifPresent(team -> team.addActivePlayer(singlePlayer));
    }

    public SinglePlayer getActivePlayer(ResourceLocation id){
        return this.activePlayers.get(id);
    }

    public SinglePlayer getActivePlayer(KnownPlayer knownPlayer){
        return getActivePlayer(knownPlayer.id());
    }

    public SinglePlayer getActivePlayer(Player player){
        return getActivePlayer(SinglePlayer.createId(player.getStringUUID()));
    }

    public ImmutableCollection<SinglePlayer> allPlayersData() {
        return ImmutableSet.copyOf(this.activePlayers.values());
    }

    public void removePlayerFromActive(ResourceLocation id){
        var player = this.activePlayers.remove(id);
        player.getTeam().ifPresent(team -> team.removeActivePlayer(player));
    }

    public void removePlayerFromActive(Player player){
        removePlayerFromActive(SinglePlayer.createId(player.getStringUUID()));
    }

    public void addActivePlayer(Player player, File playerFile) {
        this.addActivePlayer(SinglePlayer.fromFile(playerFile, player));
    }

    public void addKnownPlayer(Player player){
        if(this.knownPlayers.add(KnownPlayer.fromPlayer(player))) this.setDirty();
    }

    public void playerLoggedOut(Player player) {
        var singlePlayer = getActivePlayer(player);
        singlePlayer.loggedOut();
    }
}
