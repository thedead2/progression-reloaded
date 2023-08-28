package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraftforge.eventbus.api.Cancelable;


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

        private final SinglePlayer player;

        private final ProgressionLevel previousLevel;


        public UpdateLevelEvent(ProgressionLevel level, SinglePlayer singlePlayer, ProgressionLevel previousLevel) {
            super(level);
            this.player = singlePlayer;
            this.previousLevel = previousLevel;
        }


        public SinglePlayer getPlayer() {
            return player;
        }


        public ProgressionLevel getPreviousLevel() {
            return previousLevel;
        }
    }

    public static class UpdateLevelStatusEvent extends LevelEvent {

        private final SinglePlayer player;

        private final LevelProgress progress;


        public UpdateLevelStatusEvent(ProgressionLevel level, SinglePlayer player, LevelProgress progress) {
            super(level);
            this.player = player;
            this.progress = progress;
        }


        public SinglePlayer getPlayer() {
            return player;
        }


        public LevelProgress getProgress() {
            return progress;
        }
    }

    @Cancelable
    public static class LevelRevokedEvent extends LevelEvent {

        private final SinglePlayer player;


        public LevelRevokedEvent(ProgressionLevel level, SinglePlayer player) {
            super(level);
            this.player = player;
        }


        public SinglePlayer getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class LevelAwardEvent extends LevelEvent {

        private final SinglePlayer player;


        public LevelAwardEvent(ProgressionLevel level, SinglePlayer player) {
            super(level);
            this.player = player;
        }


        public SinglePlayer getPlayer() {
            return player;
        }
    }
}
