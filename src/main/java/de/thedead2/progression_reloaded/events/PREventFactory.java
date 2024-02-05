package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;


public abstract class PREventFactory {

    private static final IEventBus EVENT_BUS = MinecraftForge.EVENT_BUS;


    public static void onLevelChanged(ProgressionLevel newLevel, PlayerData playerData, ProgressionLevel oldLevel) {
        EVENT_BUS.post(new LevelEvent.LevelChangedEvent(newLevel, playerData, oldLevel));
    }


    public static void onPlayerSynced(PlayerData player) {
        EVENT_BUS.post(new UpdateStatusEvent.PlayerSyncedEvent(player));
    }


    public static boolean onLevelRevoke(PlayerData player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelRevokedEvent(level, player));
    }


    public static boolean onLevelAward(PlayerData player, ProgressionLevel level) {
        return EVENT_BUS.post(new LevelEvent.LevelAwardEvent(level, player));
    }


    public static boolean onQuestAward(ProgressionQuest quest, QuestProgress questProgress, PlayerData player) {
        return EVENT_BUS.post(new QuestEvent.QuestAwardEvent(quest, questProgress, player));
    }


    public static boolean onQuestRevoke(ProgressionQuest quest, QuestProgress questProgress, PlayerData player) {
        return EVENT_BUS.post(new QuestEvent.QuestRevokedEvent(quest, questProgress, player));
    }


    public static <T> boolean onTriggerFiring(SimpleCriterionTrigger<T> trigger, PlayerData player, T toTest, Object[] data, boolean triggerTestResult) {
        return EVENT_BUS.post(new UpdateStatusEvent.TriggerEvent(trigger, player, toTest, data, triggerTestResult));
    }


    public static void onQuestStatusChanged(ProgressionQuest quest, QuestStatus oldStatus, QuestStatus newStatus, PlayerData player) {
        EVENT_BUS.post(new QuestEvent.StatusChangedEvent(quest, player, oldStatus, newStatus));
    }


    public static void onQuestProgressChanged(ProgressionQuest quest, QuestProgress questProgress, PlayerData player) {
        EVENT_BUS.post(new QuestEvent.ProgressChangedEvent(quest, player, questProgress));
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


    public static Map<ResourceLocation, IFontReader<?>> onFontTypeRegistration() {
        RegisterEvent.RegisterFontTypesEvent event = new RegisterEvent.RegisterFontTypesEvent();
        EVENT_BUS.post(event);
        return event.getObjects();
    }


    public static void onQuestFinished(ProgressionQuest quest, QuestStatus completionStatus, PlayerData player) {
        EVENT_BUS.post(new QuestEvent.CompletionEvent(quest, player, completionStatus));
    }


    public static boolean onStatusUpdatePre(PlayerData player) {
        return EVENT_BUS.post(new UpdateStatusEvent.Pre(player));
    }


    public static void onStatusUpdatePost(PlayerData player) {
        EVENT_BUS.post(new UpdateStatusEvent.Post(player));
    }


    public static void onQuestFocusChanged(ProgressionQuest newFollowedQuest, PlayerData clientData, ProgressionQuest oldFollowedQuest) {

    }
}
