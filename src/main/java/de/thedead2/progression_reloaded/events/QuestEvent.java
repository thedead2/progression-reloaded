package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;


public abstract class QuestEvent extends Event {
    private final ProgressionQuest quest;

    private final PlayerData player;


    public QuestEvent(ProgressionQuest quest, PlayerData player) {
        this.quest = quest;
        this.player = player;
    }


    public ProgressionQuest getQuest() {
        return quest;
    }


    public PlayerData getPlayer() {
        return player;
    }


    public static class StatusChangedEvent extends QuestEvent {

        private final QuestStatus oldStatus;

        private final QuestStatus newStatus;


        public StatusChangedEvent(ProgressionQuest quest, PlayerData player, QuestStatus oldStatus, QuestStatus newStatus) {
            super(quest, player);
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
        }


        public QuestStatus getOldStatus() {
            return oldStatus;
        }


        public QuestStatus getNewStatus() {
            return newStatus;
        }
    }

    public static class ProgressChangedEvent extends QuestEvent {

        private final QuestProgress questProgress;


        public ProgressChangedEvent(ProgressionQuest quest, PlayerData player, QuestProgress questProgress) {
            super(quest, player);
            this.questProgress = questProgress;
        }


        public QuestProgress getQuestProgress() {
            return questProgress;
        }
    }

    public static class CompletionEvent extends QuestEvent {

        private final QuestStatus completionStatus;


        public CompletionEvent(ProgressionQuest quest, PlayerData player, QuestStatus completionStatus) {
            super(quest, player);
            this.completionStatus = completionStatus;
        }


        public QuestStatus getCompletionStatus() {
            return completionStatus;
        }
    }

    @Cancelable
    public static class QuestAwardEvent extends QuestEvent {

        private final QuestProgress questProgress;


        public QuestAwardEvent(ProgressionQuest quest, QuestProgress questProgress, PlayerData player) {
            super(quest, player);
            this.questProgress = questProgress;
        }


        public QuestProgress getQuestProgress() {
            return questProgress;
        }
    }

    @Cancelable
    public static class QuestRevokedEvent extends QuestEvent {

        private final QuestProgress questProgress;


        public QuestRevokedEvent(ProgressionQuest quest, QuestProgress questProgress, PlayerData player) {
            super(quest, player);
            this.questProgress = questProgress;
        }


        public QuestProgress getQuestProgress() {
            return questProgress;
        }
    }
}
