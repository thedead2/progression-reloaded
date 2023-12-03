package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;


public abstract class QuestEvent extends PREventFactory.ProgressionEvent {

    private final ProgressionQuest quest;


    public QuestEvent(ProgressionQuest quest) {
        this.quest = quest;
    }


    public ProgressionQuest getQuest() {
        return quest;
    }


    @Cancelable
    public static class AwardQuestEvent extends QuestEvent {

        private final QuestProgress progress;

        private final PlayerData player;


        public AwardQuestEvent(ProgressionQuest quest, QuestProgress progress, PlayerData player) {
            super(quest);
            this.progress = progress;
            this.player = player;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public QuestProgress getProgress() {
            return progress;
        }

    }

    @Cancelable
    public static class RevokedQuestEvent extends QuestEvent {

        private final QuestProgress progress;

        private final PlayerData player;


        public RevokedQuestEvent(ProgressionQuest quest, QuestProgress questProgress, PlayerData activePlayer) {
            super(quest);
            this.progress = questProgress;
            this.player = activePlayer;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public QuestProgress getProgress() {
            return progress;
        }

    }

    public static class UpdateQuestStatusEvent extends PREventFactory.ProgressionEvent {

        private final PlayerData player;


        public UpdateQuestStatusEvent(PlayerData player) {
            this.player = player;
        }


        public PlayerData getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class TriggerEvent extends PREventFactory.ProgressionEvent {

        private final SimpleTrigger<?> trigger;

        private final PlayerData player;

        private final Object toTest;

        private final Object[] addData;


        public <T> TriggerEvent(SimpleTrigger<T> trigger, PlayerData player, T toTest, Object[] data) {
            this.trigger = trigger;
            this.player = player;
            this.toTest = toTest;
            this.addData = data;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public Object[] getAddData() {
            return addData;
        }


        public SimpleTrigger<?> getTrigger() {
            return trigger;
        }


        public Object getObjectToTest() {
            return toTest;
        }
    }
}
