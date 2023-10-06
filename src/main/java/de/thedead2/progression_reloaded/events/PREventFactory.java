package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;
import java.util.Set;


public abstract class PREventFactory {

    private static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;


    public static boolean onLevelUpdate(ProgressionLevel level, PlayerData playerData, ProgressionLevel previousLevel) {
        return EVENT_BUS.post(new LevelEvent.UpdateLevelEvent(level, playerData, previousLevel));
    }


    public static void onLevelStatusUpdate(ProgressionLevel level, PlayerData playerData, LevelProgress progress) {
        EVENT_BUS.post(new LevelEvent.UpdateLevelStatusEvent(level, playerData, progress));
    }


    public static void onLevelsSynced(ProgressionLevel level, Map<ProgressionLevel, LevelProgress> progress) {
        EVENT_BUS.post(new LevelEvent.LevelsSyncedEvent(level, progress));
    }


    public static boolean onLevelRevoke(PlayerData player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelRevokedEvent(level, player));
    }


    public static boolean onLevelAward(PlayerData player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelAwardEvent(level, player));
    }


    public static boolean onQuestAward(ProgressionQuest quest, String criterionName, QuestProgress questProgress, PlayerData player) {
        return EVENT_BUS.post(new QuestEvent.AwardQuestEvent(quest, questProgress, criterionName, player));
    }


    public static boolean onQuestRevoke(ProgressionQuest quest, String criterionName, QuestProgress questProgress, PlayerData activePlayer) {
        return EVENT_BUS.post(new QuestEvent.RevokedQuestEvent(quest, questProgress, criterionName, activePlayer));
    }


    public static <T> boolean onTriggerFiring(SimpleTrigger<T> trigger, PlayerData player, T toTest, Object[] data) {
        return EVENT_BUS.post(new QuestEvent.TriggerEvent(trigger, player, toTest, data));
    }


    public static void onQuestStatusUpdate(KnownPlayer player, Set<ProgressionQuest> activeQuests) {
        EVENT_BUS.post(new QuestEvent.UpdateQuestStatusEvent(player, activeQuests));
    }


    public static Map<ResourceLocation, ProgressionTheme> onThemeRegistration() {
        RegisterEvent.RegisterThemesEvent event = new RegisterEvent.RegisterThemesEvent();
        EVENT_BUS.post(event);
        return event.getObjects();
    }


    public static Map<ResourceLocation, ProgressionLayout> onLayoutRegistration() {
        RegisterEvent.RegisterLayoutsEvent event = new RegisterEvent.RegisterLayoutsEvent();
        EVENT_BUS.post(event);
        return event.getObjects();
    }

    public static class ProgressionEvent extends Event {

    }

}
