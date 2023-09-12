package de.thedead2.progression_reloaded.player;

import com.google.common.collect.ImmutableCollection;
import de.thedead2.progression_reloaded.client.ClientDataManager;
import de.thedead2.progression_reloaded.player.data.PlayerSaveData;
import de.thedead2.progression_reloaded.player.data.TeamSaveData;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;


public abstract class PlayerDataHandler {

    public static final int maxDaysOffline = 183;

    /**
     * Don't use teamData, playerData and progressData directly as it's not safe to use.
     * Use the Getters instead!
     **/
    @Deprecated
    @SuppressWarnings("")
    private static TeamSaveData teamSaveData = null;

    @Deprecated
    private static PlayerSaveData playerSaveData = null;


    public static void loadPlayerData(File playerDataFile, Player player) {
        getPlayerSaveData().orElseThrow().addActivePlayer((ServerPlayer) player, playerDataFile);
    }


    public static Optional<PlayerSaveData> getPlayerSaveData() {
        return Optional.ofNullable(playerSaveData);
    }


    public static void loadData(ServerLevel level) {
        var dataStorage = level.getDataStorage();
        teamSaveData = dataStorage.computeIfAbsent(TeamSaveData::load, () -> new TeamSaveData(new HashMap<>()), "teams");
        playerSaveData = dataStorage.computeIfAbsent(PlayerSaveData::load, () -> new PlayerSaveData(new HashSet<>()), "players");
    }


    public static void savePlayerData(Player player, File playerFile) {
        var singlePlayer = getActivePlayer(player);
        singlePlayer.toFile(playerFile);
    }


    public static PlayerData getActivePlayer(Player player) {
        return getActivePlayer(PlayerData.createId(player.getStringUUID()));
    }


    public static PlayerData getActivePlayer(ResourceLocation player) {
        if(!ModHelper.isRunningOnServerThread()) {
            return ClientDataManager.getInstance().getClientData();
        }
        else {
            return getPlayerSaveData().orElseThrow().getActivePlayer(player);
        }
    }


    public static PlayerTeam getTeam(String teamName) {
        return getTeamData().orElseThrow().getTeam(teamName);
    }


    public static PlayerData getActivePlayer(KnownPlayer knownPlayer) {
        return getActivePlayer(knownPlayer.id());
    }


    public static PlayerTeam getTeam(KnownPlayer player) {
        return getTeamData().orElseThrow().getTeam(player);
    }


    public static PlayerTeam getTeam(ResourceLocation id) {
        return getTeamData().orElseThrow().getTeam(id);
    }


    public static ImmutableCollection<PlayerTeam> allTeams() {
        return getTeamData().orElseThrow().allTeams();
    }


    public static Optional<TeamSaveData> getTeamData() {
        return Optional.ofNullable(teamSaveData);
    }


    public static ImmutableCollection<PlayerData> allPlayers() {
        return getPlayerSaveData().orElseThrow().allPlayersData();
    }


    public static void removeActivePlayer(Player player) {
        getPlayerSaveData().orElseThrow().removeActivePlayer(player);
    }


    public static boolean playerLastOnlineLongAgo(KnownPlayer player) {
        LocalDateTime lastOnline = player.lastOnline();
        LocalDateTime now = LocalDateTime.now();
        long diff = ChronoUnit.DAYS.between(lastOnline, now);
        if(diff > maxDaysOffline) {
            ModHelper.LOGGER.info("Clearing data of player {} as it wasn't online for a long time! Last online: {}", player.name(), lastOnline.format(ModHelper.DATE_TIME_FORMATTER));
            return true;
        }
        else {
            return false;
        }
    }
}
