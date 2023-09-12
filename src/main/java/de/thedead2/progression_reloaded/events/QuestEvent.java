package de.thedead2.progression_reloaded.events;

import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;


public abstract class QuestEvent extends ModEvents.ProgressionEvent {

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

        private final String criterionName;

        private final PlayerData player;


        public AwardQuestEvent(ProgressionQuest quest, QuestProgress progress, String criterionName, PlayerData player) {
            super(quest);
            this.progress = progress;
            this.criterionName = criterionName;
            this.player = player;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public QuestProgress getProgress() {
            return progress;
        }


        public String getCriterionName() {
            return criterionName;
        }
    }

    @Cancelable
    public static class RevokedQuestEvent extends QuestEvent {

        private final QuestProgress progress;

        private final String criterionName;

        private final PlayerData player;


        public RevokedQuestEvent(ProgressionQuest quest, QuestProgress questProgress, String criterionName, PlayerData activePlayer) {
            super(quest);
            this.progress = questProgress;
            this.criterionName = criterionName;
            this.player = activePlayer;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public QuestProgress getProgress() {
            return progress;
        }


        public String getCriterionName() {
            return criterionName;
        }
    }

    public static class UpdateQuestStatusEvent extends ModEvents.ProgressionEvent {

        private final KnownPlayer player;

        private final ImmutableSet<ProgressionQuest> activeQuests;


        public UpdateQuestStatusEvent(KnownPlayer player, Set<ProgressionQuest> activeQuests) {
            this.player = player;
            this.activeQuests = ImmutableSet.copyOf(activeQuests);
        }


        public KnownPlayer getPlayer() {
            return player;
        }


        public ImmutableSet<ProgressionQuest> getActiveQuests() {
            return activeQuests;
        }
    }

    @Cancelable
    public static class TriggerEvent extends ModEvents.ProgressionEvent {

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
