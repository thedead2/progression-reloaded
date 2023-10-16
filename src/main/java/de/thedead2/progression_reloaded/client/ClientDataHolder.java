package de.thedead2.progression_reloaded.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.overlays.LevelProgressOverlay;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class ClientDataHolder {

    private final Map<ProgressionLevel, LevelProgress> levelProgress;

    private final Set<ProgressionQuest> activeQuests;

    private final Map<ProgressionQuest, QuestProgress> questProgress;

    private PlayerData clientData;

    private ProgressionLevel playerLevel;

    private boolean setOverlay = true;


    ClientDataHolder() {
        this.clientData = null;
        this.playerLevel = null;
        this.levelProgress = new HashMap<>();
        this.activeQuests = new HashSet<>();
        this.questProgress = new HashMap<>();
    }


    public static LocalPlayer getLocalPlayer() {
        return Minecraft.getInstance().player;
    }


    public void updateLevelProgress(ResourceLocation activeLevel, Map<ResourceLocation, LevelProgress> levelProgress) {
        this.playerLevel = ModRegistries.LEVELS.get().getValue(activeLevel);
        this.levelProgress.clear();
        CollectionHelper.convertMapKeys(levelProgress, this.levelProgress, id -> ModRegistries.LEVELS.get().getValue(id));
        if(setOverlay) {
            ModClientInstance.getInstance().getModRenderer().setProgressOverlay(GuiFactory.createLevelOverlay(this.playerLevel.getDisplay(), this.getCurrentLevelProgress()));
            setOverlay = false;
        }
        ModClientInstance.getInstance().getModRenderer().updateProgressOverlay(LevelProgressOverlay.class, this.getCurrentLevelProgress());
        PREventFactory.onLevelsSynced(this.playerLevel, this.levelProgress);
    }


    public void updateQuestProgress(Set<ResourceLocation> activeQuests, Map<ResourceLocation, QuestProgress> questProgress) {
        this.activeQuests.clear();
        this.questProgress.clear();
        CollectionHelper.convertCollection(activeQuests, this.activeQuests, id -> ModRegistries.QUESTS.get().getValue(id));
        CollectionHelper.convertMapKeys(questProgress, this.questProgress, id -> ModRegistries.QUESTS.get().getValue(id));
    }


    public PlayerData getClientData() {
        return clientData;
    }


    public void setClientData(PlayerData clientData) {
        this.clientData = clientData;
    }


    public ProgressionLevel getCurrentLevel() {
        return playerLevel;
    }


    public ImmutableMap<ProgressionQuest, QuestProgress> getQuestProgress() {
        return ImmutableMap.copyOf(this.questProgress);
    }


    public ImmutableMap<ProgressionLevel, LevelProgress> getLevelProgress() {
        return ImmutableMap.copyOf(this.levelProgress);
    }


    public ImmutableSet<ProgressionQuest> getActiveQuests() {
        return ImmutableSet.copyOf(this.activeQuests);
    }


    public LevelProgress getCurrentLevelProgress() {
        return this.levelProgress.get(this.playerLevel);
    }
}
