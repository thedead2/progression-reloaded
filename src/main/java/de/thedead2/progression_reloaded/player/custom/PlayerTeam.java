package de.thedead2.progression_reloaded.player.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.ProgressionLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerTeam {

    private final String teamName;
    private ProgressionLevel progressionLevel;
    private final Set<PlayerData> members = new HashSet<>();

    public PlayerTeam(String teamName, ProgressionLevel progressionLevel) {
        this.teamName = teamName;
        this.progressionLevel = progressionLevel;
    }

    public void addPlayer(PlayerData player){
        members.add(player);
    }

    public void removePlayer(PlayerData player){
        members.remove(player);
    }

    public boolean isPlayerInTeam(PlayerData player){
        return members.contains(player);
    }

    public Set<PlayerData> getMembers() {
        return members;
    }

    public String getTeamName() {
        return teamName;
    }

    public void updateProgressionLevel(ProgressionLevel level){
        this.progressionLevel = level;
        this.members.forEach(player -> player.updateProgressionLevel(level));
    }

    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonElements = new JsonArray();
        members.forEach(player -> jsonElements.add(player.getPlayer().getStringUUID()));
        jsonObject.add(teamName, jsonElements);
        return jsonObject;
    }

    /*public static PlayerTeam fromJson(JsonObject jsonObject){
        jsonObject.asMap().forEach((s, jsonElement) -> {
            Set<Player> players = new HashSet<>();
            jsonElement.getAsJsonArray().forEach(jsonElement1 -> );
        });
    }*/
}
