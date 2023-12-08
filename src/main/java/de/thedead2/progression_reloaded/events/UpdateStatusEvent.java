package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;


public abstract class UpdateStatusEvent extends Event {

    private final PlayerData player;


    public UpdateStatusEvent(PlayerData player) {
        this.player = player;
    }


    public PlayerData getPlayer() {
        return player;
    }


    @Cancelable
    public static class Pre extends UpdateStatusEvent {

        public Pre(PlayerData player) {
            super(player);
        }
    }

    public static class Post extends UpdateStatusEvent {

        public Post(PlayerData player) {
            super(player);
        }
    }

    /**
     * Fires only on the logical Client!
     */
    public static class PlayerSyncedEvent extends UpdateStatusEvent {

        public PlayerSyncedEvent(PlayerData player) {
            super(player);
        }
    }

    @Cancelable
    public static class TriggerEvent extends Event {

        private final SimpleTrigger<?> trigger;

        private final PlayerData player;

        private final Object toTest;

        private final Object[] addData;

        private final boolean triggerTestResult;


        public <T> TriggerEvent(SimpleTrigger<T> trigger, PlayerData player, T toTest, Object[] data, boolean triggerTestResult) {
            this.trigger = trigger;
            this.player = player;
            this.toTest = toTest;
            this.addData = data;
            this.triggerTestResult = triggerTestResult;
        }


        public SimpleTrigger<?> getTrigger() {
            return trigger;
        }


        public PlayerData getPlayer() {
            return player;
        }


        public Object getObjectToTest() {
            return toTest;
        }


        public Object[] getAddData() {
            return addData;
        }


        public boolean getTriggerTestResult() {
            return triggerTestResult;
        }
    }
}
