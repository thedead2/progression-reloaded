package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Set;

public abstract class ModEvents {
    private static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;
    public static boolean onLevelUpdate(ProgressionLevel level, SinglePlayer singlePlayer, ProgressionLevel previousLevel) {
        return EVENT_BUS.post(new LevelEvent.UpdateLevelEvent(level, singlePlayer, previousLevel));
    }

    public static void onLevelStatusUpdate(ProgressionLevel level, SinglePlayer singlePlayer, LevelProgress progress) {
        EVENT_BUS.post(new LevelEvent.UpdateLevelStatusEvent(level, singlePlayer, progress));
    }

    public static boolean onLevelRevoke(SinglePlayer player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelRevokedEvent(level, player));
    }

    public static boolean onLevelAward(SinglePlayer player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelAwardEvent(level, player));
    }

    public static boolean onQuestAward(ProgressionQuest quest, String criterionName, QuestProgress questProgress, SinglePlayer player) {
        return EVENT_BUS.post(new QuestEvent.AwardQuestEvent(quest, questProgress, criterionName, player));
    }

    public static boolean onQuestRevoke(ProgressionQuest quest, String criterionName, QuestProgress questProgress, SinglePlayer activePlayer) {
        return EVENT_BUS.post(new QuestEvent.RevokedQuestEvent(quest, questProgress, criterionName, activePlayer));
    }

    public static <T> boolean onTriggerFiring(SimpleTrigger<T> trigger, SinglePlayer player, T toTest, Object[] data) {
        return EVENT_BUS.post(new QuestEvent.TriggerEvent(trigger, player, toTest, data));
    }

    public static void onQuestStatusUpdate(KnownPlayer player, Set<ProgressionQuest> activeQuests) {
        EVENT_BUS.post(new QuestEvent.UpdateQuestStatusEvent(player, activeQuests));
    }

    public static class ProgressionEvent extends Event { }
}
