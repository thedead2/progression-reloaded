package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.player.data.AbilityStats;
import de.thedead2.progression_reloaded.player.data.CustomStats;
import de.thedead2.progression_reloaded.player.data.Points;

public abstract class PlayerDataCommon {
    protected AbilityStats abilities = new AbilityStats();
    protected CriteriaMappings mappings = new CriteriaMappings();
    protected CustomStats custom = new CustomStats();
    protected PlayerTeam team; //To be set on connect
    protected Points points = new Points();

    public CriteriaMappings getMappings() {
        return mappings;
    }

    public AbilityStats getAbilities() {
        return abilities;
    }

    public CustomStats getCustomStats() {
        return custom;
    }
    
    public Points getPoints() {
        return points;
    }

    protected void markDirty() {
        de.thedead2.progression_reloaded.ProgressionReloaded.data.markDirty();
    }

    public PlayerTeam getTeam() {
        return team;
    }

    public void setTeam(PlayerTeam team) {
        this.team = team;
    }
}
