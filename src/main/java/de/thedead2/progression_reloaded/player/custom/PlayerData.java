package de.thedead2.progression_reloaded.player.custom;

import de.thedead2.progression_reloaded.data.ProgressionLevel;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class PlayerData {

    private final String playerName;

    private final PlayerTeam team;

    private final UUID uuid;

    private final Player player;

    private ProgressionLevel progressionLevel;

    public PlayerData(Player player, ProgressionLevel progressionLevel) {
        this(null, player, progressionLevel);
    }

    public PlayerData(PlayerTeam team, Player player, ProgressionLevel progressionLevel) {
        this.playerName = player.getScoreboardName();
        this.team = team;
        this.uuid = player.getUUID();
        this.player = player;
        this.progressionLevel = progressionLevel;
    }


    public Player getPlayer() {
        return player;
    }

    public Optional<PlayerTeam> getTeam() {
        return Optional.ofNullable(team);
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isInTeam(){
        return team != null;
    }

    public boolean isInTeam(PlayerTeam team){
        return team.equals(this.team);
    }

    public void updateProgressionLevel(ProgressionLevel level){
        this.progressionLevel = level;
    }

    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }

    public boolean hasProgressionLevel(ProgressionLevel other){
        return this.progressionLevel.equals(other) || this.progressionLevel.contains(other);
    }
}
