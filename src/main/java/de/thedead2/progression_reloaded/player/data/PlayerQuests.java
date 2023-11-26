package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.QuestManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class PlayerQuests {

    private final Supplier<PlayerData> playerData;

    private final Set<ProgressionQuest> activeQuests = Sets.newHashSet();

    private final Set<ProgressionQuest> completedQuests = Sets.newHashSet();

    private final Map<ProgressionQuest, QuestProgress> questProgress = Maps.newHashMap();


    private PlayerQuests(Supplier<PlayerData> playerData, Set<ProgressionQuest> activeQuests, Set<ProgressionQuest> completedQuests, Map<ProgressionQuest, QuestProgress> questProgress) {
        this.playerData = playerData;
        this.activeQuests.addAll(activeQuests);
        this.completedQuests.addAll(completedQuests);
        this.questProgress.putAll(questProgress);
    }


    public PlayerQuests(Supplier<PlayerData> playerData) {
        this.playerData = playerData;
    }


    public static PlayerQuests loadFromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        Set<ProgressionQuest> activeQuests = CollectionHelper.loadFromNBT(new HashSet<>(), tag.getList("activeQuests", 0), tag1 -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag1.getAsString())));
        Set<ProgressionQuest> completedQuests = CollectionHelper.loadFromNBT(new HashSet<>(), tag.getList("completedQuests", 0), tag1 -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag1.getAsString())));
        Map<ProgressionQuest, QuestProgress> questProgress = CollectionHelper.loadFromNBT(tag.getCompound("questProgress"), s -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(s)), tag1 -> QuestProgress.loadFromCompoundTag((CompoundTag) tag1));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), activeQuests, completedQuests, questProgress);
    }


    public static PlayerQuests loadFromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        Set<ProgressionQuest> activeQuests = buf.readCollection(Sets::newHashSetWithExpectedSize, buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()));
        Set<ProgressionQuest> completedQuests = buf.readCollection(Sets::newHashSetWithExpectedSize, buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()));
        Map<ProgressionQuest, QuestProgress> questProgress = buf.readMap(buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()), QuestProgress::fromNetwork);

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), activeQuests, completedQuests, questProgress);
    }


    public @NotNull CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("player", this.playerData.get().getUUID());
        tag.put("activeQuests", CollectionHelper.saveToNBT(this.activeQuests, quest -> StringTag.valueOf(quest.getId().toString())));
        tag.put("completedQuests", CollectionHelper.saveToNBT(this.completedQuests, quest -> StringTag.valueOf(quest.getId().toString())));
        tag.put("questProgress", CollectionHelper.saveToNBT(this.questProgress, quest -> quest.getId().toString(), QuestProgress::saveToCompoundTag));

        return tag;
    }


    public void saveToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeCollection(this.activeQuests, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()));
        buf.writeCollection(this.completedQuests, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()));
        buf.writeMap(this.questProgress, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()), (buf1, progress) -> progress.toNetwork(buf1));
    }


    public void forEachCompleted(Consumer<ProgressionQuest> questConsumer) {
        this.completedQuests.forEach(questConsumer);
    }


    public void forEachActive(Consumer<ProgressionQuest> questConsumer) {
        this.activeQuests.forEach(questConsumer);
    }


    public QuestProgress getOrStartProgress(ProgressionQuest quest) {
        QuestProgress questProgress = this.questProgress.get(quest);
        if(questProgress == null) {
            questProgress = new QuestProgress(quest);
            this.startProgress(quest, questProgress);
        }
        return questProgress;
    }


    private void startProgress(ProgressionQuest quest, QuestProgress questProgress) {
        questProgress.updateProgress();
        this.questProgress.putIfAbsent(quest, questProgress);
    }


    public void updateQuestStatus(QuestManager questManager) {
        this.activeQuests.clear();
        this.completedQuests.clear();
        this.activeQuests.addAll(questManager.searchQuestsForActive(this.playerData.get()));
        this.completedQuests.addAll(questManager.searchQuestsForComplete(this.playerData.get()));
    }


    public void copy(PlayerQuests playerQuests) {
        if(this.equals(playerQuests)) {
            return;
        }

        this.activeQuests.clear();
        this.activeQuests.addAll(playerQuests.activeQuests);
        this.completedQuests.clear();
        this.completedQuests.addAll(playerQuests.completedQuests);
        this.questProgress.clear();
        this.questProgress.putAll(playerQuests.questProgress);
    }


    public ImmutableSet<ProgressionQuest> getActiveQuests() {
        return ImmutableSet.copyOf(this.activeQuests);
    }


    public ImmutableSet<ProgressionQuest> getCompletedQuests() {
        return ImmutableSet.copyOf(this.completedQuests);
    }
}
