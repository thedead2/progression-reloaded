package de.thedead2.progression_reloaded.data.trigger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.player.SinglePlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class SimpleTrigger {
    private final Map<ProgressionLevel, Set<Listener<? extends SimpleTrigger>>> players = Maps.newIdentityHashMap();
    public void addListener(ProgressionLevel progressionLevel, Listener<SimpleTrigger> listener){
        this.players.computeIfAbsent(progressionLevel, (level) -> Sets.newHashSet()).add(listener);
    }

    public void removeListener(ProgressionLevel progressionLevel, Listener<SimpleTrigger> listener){
        Set<Listener<? extends SimpleTrigger>> set = this.players.get(progressionLevel);
        if (set != null) {
            set.remove(listener);
            if (set.isEmpty()) {
                this.players.remove(progressionLevel);
            }
        }
    }

    protected void trigger(SinglePlayer player, Predicate<SinglePlayer> triggerTest) {
        ProgressionLevel progressionLevel = player.getProgressionLevel();
        Set<Listener<? extends SimpleTrigger>> set = this.players.get(progressionLevel);
        if (set != null && !set.isEmpty()) {
            List<Listener<? extends SimpleTrigger>> list = Lists.newArrayList();

            for(Listener<? extends SimpleTrigger> listener : set) {
                //SimpleTrigger trigger = listener.getTrigger();
                if (triggerTest.test(player)) {
                    list.add(listener);
                }
            }

            for (Listener<? extends SimpleTrigger> listener1 : list) {
                listener1.award(progressionLevel);
            }
        }
    }

    public static class Listener<T extends SimpleTrigger> {
        private final T trigger;
        private final ProgressionQuest quest;
        private final String criterion;

        public Listener(T trigger, ProgressionQuest quest, String criterionName) {
            this.trigger = trigger;
            this.quest = quest;
            this.criterion = criterionName;
        }

        public T getTrigger() {
            return this.trigger;
        }

        public void award(ProgressionLevel progressionLevel) {
            progressionLevel.award(this.quest, this.criterion);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Listener<?> listener = (Listener<?>) o;
            return Objects.equal(trigger, listener.trigger) && Objects.equal(quest, listener.quest) && Objects.equal(criterion, listener.criterion);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(trigger, quest, criterion);
        }
    }
}
