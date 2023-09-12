package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;


public class PlayerSaveData extends SavedData {

    private final Set<KnownPlayer> knownPlayers = new HashSet<>();

    private final Map<ResourceLocation, PlayerData> activePlayers = new HashMap<>();


    public PlayerSaveData(Collection<KnownPlayer> knownPlayers) {
        this.knownPlayers.addAll(knownPlayers);
        this.knownPlayers.removeIf(PlayerDataHandler::playerLastOnlineLongAgo);
        this.setDirty();
    }


    public static PlayerSaveData load(CompoundTag tag) {
        Set<KnownPlayer> knownPlayers = new HashSet<>();
        tag.getAllKeys().forEach(s -> {
            knownPlayers.add(KnownPlayer.fromCompoundTag(tag.getCompound(s)));
        });
        return new PlayerSaveData(knownPlayers);
    }


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        knownPlayers.forEach(knownPlayer -> tag.put(knownPlayer.id().toString(), knownPlayer.toCompoundTag()));
        return tag;
    }


    public PlayerData getActivePlayer(ResourceLocation id) {
        return this.activePlayers.get(id);
    }


    public ImmutableCollection<PlayerData> allPlayersData() {
        return ImmutableSet.copyOf(this.activePlayers.values());
    }


    public void removeActivePlayer(Player player) {
        removeActivePlayer(PlayerData.createId(player.getStringUUID()));
    }


    public void removeActivePlayer(ResourceLocation id) {
        var player = this.activePlayers.remove(id);
        player.getTeam().ifPresent(team -> team.removeActivePlayer(player));
    }


    public void addActivePlayer(ServerPlayer player, File playerFile) {
        this.addActivePlayer(PlayerData.fromFile(playerFile, player));
    }


    private void addActivePlayer(PlayerData playerData) {
        this.activePlayers.put(playerData.getId(), playerData);
        this.addKnownPlayer(playerData.getServerPlayer());
        playerData.getTeam().ifPresent(team -> team.addActivePlayer(playerData));
    }


    private void addKnownPlayer(Player player) {
        if(this.knownPlayers.add(KnownPlayer.fromPlayer(player))) {
            this.setDirty();
        }
    }
}
