package de.thedead2.progression_reloaded.player;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class PlayerDataManager {

    public static final int maxDaysOffline = 183;

    /**
     * Don't use teamData, playerData and progressData directly as it's not safe to use.
     * Use the Getters instead!
     **/
    private static TeamSaveData teamSaveData = null;

    private static final Map<UUID, PlayerData> PLAYERS = Maps.newHashMap();


    public static void loadPlayerData(File playerDataFile, ServerPlayer player) {
        PlayerData playerData = PlayerData.loadFromFile(playerDataFile, player);
        PLAYERS.put(playerData.getUUID(), playerData);
        playerData.getTeam().ifPresent(team -> team.addActivePlayer(playerData));
    }


    public static void loadData(ServerLevel level) {
        var dataStorage = level.getDataStorage();
        teamSaveData = dataStorage.computeIfAbsent(TeamSaveData::load, () -> new TeamSaveData(new HashMap<>()), "teams");
    }


    public static void savePlayerData(Player player, File playerFile) {
        var playerData = getPlayerData(player);
        playerData.saveToFile(playerFile);
    }


    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUUID());
    }


    //TODO: When accessing from render/ client thread no online players are available?!
    public static PlayerData getPlayerData(UUID playerId) {
        if(ModHelper.isRunningOnServerThread()) {
            return PLAYERS.get(playerId);
        }
        else {
            PlayerData clientData = ModClientInstance.getInstance().getClientData();
            if(clientData.getUUID().equals(playerId)) {
                return clientData;
            }
            else {
                throw new IllegalArgumentException("Tried to access clientData for other local player!");
            }
        }
    }


    public static void updateProgressionLevel(PlayerData player, ProgressionLevel level) {
        if(level == null) {
            return;
        }
        player.updateProgressionLevel(level);
        player.getTeam().ifPresent(team -> team.updateProgressionLevel(level, player));
    }


    public static PlayerTeam getTeam(String teamName) {
        return getTeamData().orElseThrow().getTeam(teamName);
    }


    public static void addTeam(PlayerTeam team) {
        getTeamData().ifPresent(teamData -> teamData.addTeam(team));
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


    private static Optional<TeamSaveData> getTeamData() {
        return Optional.ofNullable(teamSaveData);
    }


    public static boolean deleteTeam(ResourceLocation teamId) {
        AtomicBoolean bool = new AtomicBoolean(false);
        getTeamData().ifPresent(teamData -> bool.set(teamData.removeTeam(teamId)));
        return bool.get();
    }


    public static boolean deleteTeam(PlayerTeam team) {
        AtomicBoolean bool = new AtomicBoolean(false);
        getTeamData().ifPresent(teamData -> bool.set(teamData.removeTeam(team)));
        return bool.get();
    }


    public static boolean clearTeams() {
        AtomicBoolean bool = new AtomicBoolean(false);
        getTeamData().ifPresent(teamData -> {
            teamData.clearAll();
            bool.set(true);
        });
        return bool.get();
    }


    public static ImmutableCollection<PlayerData> allPlayers() {
        return ImmutableSet.copyOf(PLAYERS.values());
    }


    public static void clearPlayerData(Player player) {
        clearPlayerData(player.getUUID());
    }


    public static void clearPlayerData(UUID playerId) {
        var player = PLAYERS.remove(playerId);
        LevelManager.getInstance().getQuestManager().stopListening(player);
        player.getTeam().ifPresent(team -> team.removeActivePlayer(player));
    }


    public static void ensureQuestsSynced(PlayerData player) {
        PlayerQuests playerQuests = player.getQuestData();
        player.getTeam().ifPresent(team -> {
            team.forEachMember(member -> {
                PlayerData activeMember = getPlayerData(member);

                if(activeMember != null) {
                    activeMember.copyQuestProgress(playerQuests);
                }
                else {
                    team.queueQuestSync(member, playerQuests);
                }
            });
        });
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


    public static PlayerData getPlayerData(KnownPlayer player) {
        return getPlayerData(player.uuid());
    }


    private static class TeamSaveData extends SavedData {

        private final Map<ResourceLocation, PlayerTeam> teams = new HashMap<>();


        public TeamSaveData(Map<ResourceLocation, PlayerTeam> teams) {
            this.teams.putAll(teams);
            this.setDirty();
        }


        public static TeamSaveData load(CompoundTag tag) {
            Map<ResourceLocation, PlayerTeam> teamMap = CollectionHelper.loadFromNBT(tag, ResourceLocation::new, tag1 -> PlayerTeam.fromCompoundTag((CompoundTag) tag1));

            return new TeamSaveData(teamMap);
        }


        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
            return CollectionHelper.saveToNBT(this.teams, ResourceLocation::toString, PlayerTeam::toCompoundTag);
        }


        public void addTeam(PlayerTeam team) {
            this.teams.put(team.getId(), team);
            this.setDirty();
        }


        public PlayerTeam getTeam(KnownPlayer player) {
            return this.teams.values().stream().filter(playerTeam -> playerTeam.isPlayerInTeam(player)).findAny().orElse(
                    null);
        }


        public PlayerTeam getTeam(String teamName) {
            return getTeam(PlayerTeam.createId(teamName));
        }


        public PlayerTeam getTeam(ResourceLocation id) {
            return this.teams.get(id);
        }


        public ImmutableCollection<PlayerTeam> allTeams() {
            return ImmutableSet.copyOf(this.teams.values());
        }


        public boolean removeTeam(PlayerTeam team) {
            return removeTeam(team.getId());
        }


        public boolean removeTeam(ResourceLocation id) {
            this.setDirty();
            PlayerTeam team = this.teams.get(id);
            if(team != null) {
                team.getActiveMembers().forEach(singlePlayer -> singlePlayer.setTeam(null));
            }
            this.teams.remove(id);
            return team != null;
        }


        public void clearAll() {
            this.teams.clear();
            this.setDirty();
        }
    }
}
