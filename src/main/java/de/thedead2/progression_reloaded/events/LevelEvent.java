package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Map;


public abstract class LevelEvent extends ModEvents.ProgressionEvent {

    private final ProgressionLevel level;


    public LevelEvent(ProgressionLevel level) {
        this.level = level;
    }


    public ProgressionLevel getLevel() {
        return level;
    }


    @Cancelable
    public static class UpdateLevelEvent extends LevelEvent {

        private final PlayerData player;

        private final ProgressionLevel previousLevel;


        public UpdateLevelEvent(ProgressionLevel level, PlayerData playerData, ProgressionLevel previousLevel) {
            super(level);
            this.player = playerData;
            this.previousLevel = previousLevel;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public ProgressionLevel getPreviousLevel() {
            return previousLevel;
        }
    }

    public static class UpdateLevelStatusEvent extends LevelEvent {

        private final PlayerData player;

        private final LevelProgress progress;


        public UpdateLevelStatusEvent(ProgressionLevel level, PlayerData player, LevelProgress progress) {
            super(level);
            this.player = player;
            this.progress = progress;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public LevelProgress getProgress() {
            return progress;
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

    /**
     * Fired when the levels have been synced with the client. Fires only on the client!
     */
    public static class LevelsSyncedEvent extends LevelEvent {

        private final Map<ProgressionLevel, LevelProgress> levelProgress;


        public LevelsSyncedEvent(ProgressionLevel level, Map<ProgressionLevel, LevelProgress> progress) {
            super(level);
            this.levelProgress = progress;
        }


        public Map<ProgressionLevel, LevelProgress> getLevelProgress() {
            return levelProgress;
        }
    }
}
