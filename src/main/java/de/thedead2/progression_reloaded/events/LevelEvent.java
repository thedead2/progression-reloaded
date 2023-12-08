package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;


public abstract class LevelEvent extends Event {

    private final ProgressionLevel level;


    public LevelEvent(ProgressionLevel level) {
        this.level = level;
    }


    public ProgressionLevel getLevel() {
        return level;
    }


    /**
     * Fired when the level of a player has changed. Fires on both sides!
     **/
    public static class LevelChangedEvent extends LevelEvent {

        private final PlayerData player;

        private final ProgressionLevel oldLevel;


        public LevelChangedEvent(ProgressionLevel newLevel, PlayerData playerData, ProgressionLevel oldLevel) {
            super(newLevel);
            this.player = playerData;
            this.oldLevel = oldLevel;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public ProgressionLevel getOldLevel() {
            return oldLevel;
        }
    }

    @Cancelable
    public static class LevelRevokedEvent extends LevelEvent {

        private final PlayerData player;


        public LevelRevokedEvent(ProgressionLevel level, PlayerData player) {
            super(level);
            this.player = player;
        }


        public PlayerData getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class LevelAwardEvent extends LevelEvent {

        private final PlayerData player;


        public LevelAwardEvent(ProgressionLevel level, PlayerData player) {
            super(level);
            this.player = player;
        }


        public PlayerData getPlayer() {
            return player;
        }
    }
}
