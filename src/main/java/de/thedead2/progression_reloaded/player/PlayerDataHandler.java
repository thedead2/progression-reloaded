package de.thedead2.progression_reloaded.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import de.thedead2.progression_reloaded.player.data.PlayerData;
import de.thedead2.progression_reloaded.player.data.ProgressData;
import de.thedead2.progression_reloaded.player.data.TeamData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public abstract class PlayerDataHandler {

    /**
     * Don't use teamData, playerData and progressData directly as it's not safe to use.
     * Use the Getters instead!
     * **/
    private static TeamData teamData = null;
    private static PlayerData playerData = null;
    private static ProgressData progressData = null;

    public static void loadPlayerData(File playerDataFile, Player player){
        getPlayerData().orElseThrow().addActivePlayer((ServerPlayer) player, playerDataFile);
    }

    public static void loadData(ServerLevel level){
        var dataStorage = level.getDataStorage();
        teamData = dataStorage.computeIfAbsent(TeamData::load, () -> new TeamData(new HashMap<>()), "teams");
        playerData = dataStorage.computeIfAbsent(PlayerData::load, () -> new PlayerData(new HashSet<>()), "players");
        progressData = dataStorage.computeIfAbsent(ProgressData::load, () -> new ProgressData(HashMultimap.create(), HashMultimap.create(), new HashMap<>(), new HashMap<>()), "progress");
    }

    public static void savePlayerData(Player player, File playerFile) {
        var playerData = getPlayerData().orElseThrow();
        var singlePlayer = playerData.getActivePlayer(player);
        singlePlayer.toFile(playerFile);
        if (singlePlayer.isOffline()) playerData.removePlayerFromActive(player);
    }

    public static Optional<TeamData> getTeamData(){
        return Optional.ofNullable(teamData);
    }

    public static Optional<PlayerData> getPlayerData(){
        return Optional.ofNullable(playerData);
    }
    public static Optional<ProgressData> getProgressData(){
        return Optional.ofNullable(progressData);
    }

    public static SinglePlayer getActivePlayer(KnownPlayer knownPlayer) {
        return getPlayerData().orElseThrow().getActivePlayer(knownPlayer);
    }
    public static SinglePlayer getActivePlayer(Player player) {
        return getPlayerData().orElseThrow().getActivePlayer(player);
    }

    public static PlayerTeam getTeam(String teamName) {
        return getTeamData().orElseThrow().getTeam(teamName);
    }
    public static PlayerTeam getTeam(ResourceLocation id) {
        return getTeamData().orElseThrow().getTeam(id);
    }

    public static ImmutableCollection<PlayerTeam> allTeams() {
        return getTeamData().orElseThrow().allTeams();
    }

    public static SinglePlayer getActivePlayer(ResourceLocation player) {
        return getPlayerData().orElseThrow().getActivePlayer(player);
    }

    public static ImmutableCollection<SinglePlayer> allPlayers() {
        return getPlayerData().orElseThrow().allPlayersData();
    }
}
