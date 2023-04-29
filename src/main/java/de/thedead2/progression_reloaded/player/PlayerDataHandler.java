package de.thedead2.progression_reloaded.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

public abstract class PlayerDataHandler {

    private static TeamData teamData = null;
    private static PlayerData playerData = null;

    public static void loadPlayerData(File playerDataFile, Player player){
        getPlayerData().orElseThrow().addActivePlayer(player, playerDataFile);
    }

    public static void loadData(ServerLevel level){
        var dataStorage = level.getDataStorage();
        teamData = dataStorage.computeIfAbsent(TeamData::load, () -> new TeamData(Collections.emptyMap()), "teams");
        playerData = dataStorage.computeIfAbsent(PlayerData::load, () -> new PlayerData(Collections.emptySet()), "players");
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
}
