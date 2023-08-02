package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProgressData extends SavedData {
    private final Multimap<KnownPlayer, ProgressionQuest> activeQuests;
    private final Multimap<KnownPlayer, QuestProgress> questProgress;
    private final Map<ProgressionLevel, LevelProgress> levelProgress;
    private final Map<KnownPlayer, ProgressionLevel> playerLevels;

    public ProgressData(Multimap<KnownPlayer, ProgressionQuest> activeQuests, Multimap<KnownPlayer, QuestProgress> questProgress, Map<ProgressionLevel, LevelProgress> levelProgress, Map<KnownPlayer, ProgressionLevel> playerLevels) {
        this.activeQuests = activeQuests;
        this.questProgress = questProgress;
        this.levelProgress = levelProgress;
        this.playerLevels = playerLevels;
        this.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag tag1 = new CompoundTag();
        this.activeQuests.keySet().forEach(knownPlayer -> {
            tag1.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            CompoundTag tag2 = new CompoundTag();
            this.activeQuests.get(knownPlayer).forEach(quest -> tag2.putBoolean(quest.getId().toString(), quest.isMainQuest()));
            tag1.put(knownPlayer.id() + "-activeQuests", tag2);
        });
        tag.put("activeQuests", tag1);

        CompoundTag tag2 = new CompoundTag();
        this.questProgress.keySet().forEach(knownPlayer -> {
            tag2.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            CompoundTag tag3 = new CompoundTag();
            this.questProgress.get(knownPlayer).forEach(questProgress1 -> tag3.put(questProgress1.getQuest().getId().toString(), questProgress1.saveToCompoundTag()));
            tag2.put(knownPlayer.id() + "-progress", tag3);
        });
        tag.put("questProgress", tag2);

        CompoundTag levelProgressTag = new CompoundTag();
        this.levelProgress.forEach((level, levelProgress1) -> levelProgressTag.put(level.getId().toString(), levelProgress1.saveToCompoundTag()));
        tag.put("levelProgress", levelProgressTag);

        CompoundTag tag4 = new CompoundTag();
        this.playerLevels.forEach((knownPlayer, level) -> {
            tag4.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            tag4.putString(knownPlayer.id() + "-level", level.getId().toString());
        });
        tag.put("playerLevels", tag4);

        return tag;
    }

    public static ProgressData load(CompoundTag tag) {
        final Multimap<KnownPlayer, ProgressionQuest> activeQuests = HashMultimap.create();
        final Multimap<KnownPlayer, QuestProgress> questProgress = HashMultimap.create();
        final Map<ProgressionLevel, LevelProgress> levelProgress = new HashMap<>();
        final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();

        CompoundTag activeQuestsTag = tag.getCompound("activeQuests");
        activeQuestsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(activeQuestsTag.getCompound(s));
            CompoundTag tag1 = activeQuestsTag.getCompound(player.id() + "-activeQuests");
            tag1.getAllKeys().forEach(s1 -> activeQuests.put(player, ModRegistries.QUESTS.get().getValue(new ResourceLocation(s1))));
        });

        CompoundTag questTag = tag.getCompound("questProgress");
        questTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(questTag.getCompound(s));
            CompoundTag tag1 = questTag.getCompound(player.id() + "-progress");
            tag1.getAllKeys().forEach(s1 -> {
                QuestProgress progress = QuestProgress.loadFromCompoundTag(tag1.getCompound(s1));
                questProgress.put(player, progress);
            });
        });

        CompoundTag levelProgressTag = tag.getCompound("levelProgress");
        levelProgressTag.getAllKeys().forEach(s -> levelProgress.put(ModRegistries.LEVELS.get().getValue(new ResourceLocation(s)), LevelProgress.loadFromCompoundTag(levelProgressTag.getCompound(s))));

        CompoundTag playerLevelsTag = tag.getCompound("playerLevels");
        playerLevelsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(playerLevelsTag.getCompound(s));
            ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(playerLevelsTag.getString(player.id() + "-level")));
            playerLevels.put(player, level);
        });

        return new ProgressData(activeQuests, questProgress, levelProgress, playerLevels);
    }

    public Multimap<KnownPlayer, QuestProgress> getQuestProgress(Collection<ProgressionQuest> quests) {
        Multimap<KnownPlayer, QuestProgress> levelQuestsProgress = HashMultimap.create();
        this.questProgress.keySet().forEach(knownPlayer -> this.questProgress.get(knownPlayer).stream().filter(progress -> progress.getQuest().equalsAny(quests)).forEach(progress -> levelQuestsProgress.put(knownPlayer, progress)));
        return levelQuestsProgress;
    }

    public Multimap<KnownPlayer, ProgressionQuest> getActiveQuests(Collection<ProgressionQuest> quests) {
        Multimap<KnownPlayer, ProgressionQuest> levelQuests = HashMultimap.create();
        this.activeQuests.keySet().forEach(knownPlayer -> this.activeQuests.get(knownPlayer).stream().filter(quest -> quest.equalsAny(quests)).forEach(quest -> levelQuests.put(knownPlayer, quest)));
        return levelQuests;
    }

    public void updateQuestProgressData(Multimap<SinglePlayer, QuestProgress> mainQuestsProgress, Multimap<SinglePlayer, QuestProgress> sideQuestsProgress){
        final Multimap<KnownPlayer, QuestProgress> questsProgress = HashMultimap.create();
        mainQuestsProgress.keySet().forEach(player -> {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            questsProgress.putAll(player1, mainQuestsProgress.get(player));
        });
        sideQuestsProgress.keySet().forEach(player -> {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            questsProgress.putAll(player1, sideQuestsProgress.get(player));
        });

        questsProgress.keySet().forEach(knownPlayer -> this.questProgress.putAll(knownPlayer, questsProgress.get(knownPlayer)));
        this.setDirty();
    }

    public void updateActiveQuestsData(Multimap<SinglePlayer, ProgressionQuest> activeQuests){
        activeQuests.keySet().forEach(player -> {
            KnownPlayer player1 = KnownPlayer.fromSinglePlayer(player);
            this.activeQuests.putAll(player1, activeQuests.get(player));
        });
        this.setDirty();
    }

    public void updateLevelProgressData(Map<ProgressionLevel, LevelProgress> levelProgress){
        this.levelProgress.putAll(levelProgress);
        this.setDirty();
    }

    public void updatePlayerLevels(Map<KnownPlayer, ProgressionLevel> playerLevels){
        this.playerLevels.putAll(playerLevels);
        this.setDirty();
    }

    public Map<ProgressionLevel, LevelProgress> getLevelProgress() {
        return levelProgress;
    }

    public Map<KnownPlayer, ProgressionLevel> getPlayerLevels() {
        return playerLevels;
    }
}
