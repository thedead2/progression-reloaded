package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableMap;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ProgressData extends SavedData {

    private final Map<KnownPlayer, Set<ProgressionQuest>> activeQuests;

    private final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress;

    private final Map<ProgressionLevel, LevelProgress> levelProgress;

    private final Map<KnownPlayer, ProgressionLevel> playerLevels;

    private final Map<KnownPlayer, ProgressionLevel> levelCache;


    public ProgressData(Map<KnownPlayer, Set<ProgressionQuest>> activeQuests, Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress, Map<ProgressionLevel, LevelProgress> levelProgress, Map<KnownPlayer, ProgressionLevel> playerLevels, Map<KnownPlayer, ProgressionLevel> levelCache) {
        this.activeQuests = activeQuests;
        this.playerProgress = playerProgress;
        this.levelProgress = levelProgress;
        this.playerLevels = playerLevels;
        this.levelCache = levelCache;
        this.setDirty();
    }


    public static ProgressData load(CompoundTag tag) {
        final Map<KnownPlayer, Set<ProgressionQuest>> activeQuests = new HashMap<>();
        final Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> questProgress = new HashMap<>();
        final Map<ProgressionLevel, LevelProgress> levelProgress = new HashMap<>();
        final Map<KnownPlayer, ProgressionLevel> playerLevels = new HashMap<>();
        final Map<KnownPlayer, ProgressionLevel> levelCache = new HashMap<>();

        CompoundTag activeQuestsTag = tag.getCompound("activeQuests");
        activeQuestsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(activeQuestsTag.getCompound(s));
            CompoundTag tag1 = activeQuestsTag.getCompound(player.id() + "-activeQuests");
            Set<ProgressionQuest> quests = new HashSet<>();
            tag1.getAllKeys().forEach(s1 -> quests.add(ModRegistries.QUESTS.get().getValue(new ResourceLocation(s1))));
            activeQuests.put(player, quests);
        });

        CompoundTag questTag = tag.getCompound("questProgress");
        questTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(questTag.getCompound(s));
            CompoundTag tag1 = questTag.getCompound(player.id() + "-progress");
            Map<ProgressionQuest, QuestProgress> progressMap = new HashMap<>();
            tag1.getAllKeys().forEach(s1 -> {
                QuestProgress progress = QuestProgress.loadFromCompoundTag(tag1.getCompound(s1));
                ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(progress.getQuest());
                progressMap.put(quest, progress);
            });
            questProgress.put(player, progressMap);
        });

        CompoundTag levelProgressTag = tag.getCompound("levelProgress");
        levelProgressTag.getAllKeys().forEach(s -> levelProgress.put(ModRegistries.LEVELS.get().getValue(new ResourceLocation(s)), LevelProgress.loadFromCompoundTag(levelProgressTag.getCompound(s))));

        CompoundTag playerLevelsTag = tag.getCompound("playerLevels");
        playerLevelsTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(playerLevelsTag.getCompound(s));
            ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(playerLevelsTag.getString(player.id() + "-level")));
            playerLevels.put(player, level);
        });
        CompoundTag levelCacheTag = tag.getCompound("levelCache");
        levelCacheTag.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(levelCacheTag.getCompound(s));
            ProgressionLevel level = ModRegistries.LEVELS.get().getValue(new ResourceLocation(levelCacheTag.getString(player.id() + "-level")));
            levelCache.put(player, level);
        });

        return new ProgressData(activeQuests, questProgress, levelProgress, playerLevels, levelCache);
    }


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag tag1 = new CompoundTag();
        this.activeQuests.forEach((knownPlayer, progressionQuests) -> {
            tag1.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            CompoundTag tag2 = new CompoundTag();
            progressionQuests.forEach(quest -> tag2.putBoolean(quest.getId().toString(), quest.isMainQuest()));
            tag1.put(knownPlayer.id() + "-activeQuests", tag2);
        });
        tag.put("activeQuests", tag1);

        CompoundTag tag2 = new CompoundTag();
        this.playerProgress.forEach((knownPlayer, questProgressMap) -> {
            tag2.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            CompoundTag tag3 = new CompoundTag();
            questProgressMap.forEach((quest, progress) -> tag3.put(
                    quest.getId().toString(),
                    progress.saveToCompoundTag()
            ));
            tag2.put(knownPlayer.id() + "-progress", tag3);
        });
        tag.put("questProgress", tag2);

        CompoundTag levelProgressTag = new CompoundTag();
        this.levelProgress.forEach((level, levelProgress1) -> levelProgressTag.put(
                level.getId().toString(),
                levelProgress1.saveToCompoundTag()
        ));
        tag.put("levelProgress", levelProgressTag);

        CompoundTag tag4 = new CompoundTag();
        this.playerLevels.forEach((knownPlayer, level) -> {
            tag4.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            tag4.putString(knownPlayer.id() + "-level", level.getId().toString());
        });
        tag.put("playerLevels", tag4);

        CompoundTag tag5 = new CompoundTag();
        this.levelCache.forEach((player, level) -> {
            tag5.put(player.id() + "-player", player.toCompoundTag());
            tag5.putString(player.id() + "-level", level.getId().toString());
        });
        tag.put("levelCache", tag5);

        return tag;
    }


    public ImmutableMap<KnownPlayer, Map<ProgressionQuest, QuestProgress>> getPlayerQuestProgressData() {
        return ImmutableMap.copyOf(this.playerProgress);
    }


    public ImmutableMap<KnownPlayer, Set<ProgressionQuest>> getActivePlayerQuests() {
        return ImmutableMap.copyOf(this.activeQuests);
    }


    public void updateQuestProgressData(Map<KnownPlayer, Map<ProgressionQuest, QuestProgress>> playerProgress) {
        this.playerProgress.putAll(playerProgress);
        this.setDirty();
    }


    public void updateActiveQuestsData(Map<KnownPlayer, Set<ProgressionQuest>> activeQuests) {
        this.activeQuests.putAll(activeQuests);
        this.setDirty();
    }


    public void updateLevelProgressData(Map<ProgressionLevel, LevelProgress> levelProgress) {
        this.levelProgress.putAll(levelProgress);
        this.setDirty();
    }


    public void updatePlayerLevels(Map<KnownPlayer, ProgressionLevel> playerLevels) {
        this.playerLevels.putAll(playerLevels);
        this.setDirty();
    }


    public ImmutableMap<ProgressionLevel, LevelProgress> getLevelProgress() {
        return ImmutableMap.copyOf(this.levelProgress);
    }


    public ImmutableMap<KnownPlayer, ProgressionLevel> getPlayerLevels() {
        return ImmutableMap.copyOf(this.playerLevels);
    }


    public ImmutableMap<KnownPlayer, ProgressionLevel> getLevelCache() {
        return ImmutableMap.copyOf(this.levelCache);
    }


    public void updateLevelCache(Map<KnownPlayer, ProgressionLevel> levelCache) {
        this.levelCache.putAll(levelCache);
        this.setDirty();
    }
}
