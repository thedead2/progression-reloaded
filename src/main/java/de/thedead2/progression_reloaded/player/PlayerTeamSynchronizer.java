package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;


//TODO: Class to ensure the synchronization between each player and its team for example for level/ quest progress, rewards, new level, etc.
public class PlayerTeamSynchronizer {

    public static void updateProgressionLevel(SinglePlayer player, ProgressionLevel level) {
        if(level == null) {
            return;
        }
        player.updateProgressionLevel(level);
        player.getTeam().ifPresent(team -> team.updateProgressionLevel(level, player));
    }
}
