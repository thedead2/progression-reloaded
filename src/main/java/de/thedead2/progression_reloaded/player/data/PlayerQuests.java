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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class PlayerQuests {

    private final Supplier<PlayerData> playerData;

    private final EnumMap<ProgressionQuest.Status, Set<ProgressionQuest>> questsByStatus = Maps.newEnumMap(ProgressionQuest.Status.class);

    private final Map<ProgressionQuest, QuestProgress> questProgress = Maps.newHashMap();


    private PlayerQuests(Supplier<PlayerData> playerData, EnumMap<ProgressionQuest.Status, Set<ProgressionQuest>> questsByStatus, Map<ProgressionQuest, QuestProgress> questProgress) {
        this.playerData = playerData;
        this.questsByStatus.putAll(questsByStatus);
        this.questProgress.putAll(questProgress);
    }


    public PlayerQuests(Supplier<PlayerData> playerData) {
        this.playerData = playerData;
    }


    public static PlayerQuests loadFromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        EnumMap<ProgressionQuest.Status, Set<ProgressionQuest>> questsByStatus = CollectionHelper.loadFromNBT(integer -> Maps.newEnumMap(ProgressionQuest.Status.class), tag.getCompound("questsByStatus"), ProgressionQuest.Status::valueOf, tag1 -> CollectionHelper.loadFromNBT(Sets::newHashSetWithExpectedSize, (ListTag) tag1, tag2 -> ModRegistries.QUESTS.get()
                                                                                                                                                                                                                                                                                                                                                                 .getValue(new ResourceLocation(tag2.getAsString()))));
        Map<ProgressionQuest, QuestProgress> questProgress = CollectionHelper.loadFromNBT(tag.getCompound("questProgress"), s -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(s)), tag1 -> QuestProgress.fromNBT((CompoundTag) tag1));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), questsByStatus, questProgress);
    }


    public static PlayerQuests loadFromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        EnumMap<ProgressionQuest.Status, Set<ProgressionQuest>> questsByStatus = buf.readMap(integer -> Maps.newEnumMap(ProgressionQuest.Status.class), buf1 -> buf1.readEnum(ProgressionQuest.Status.class), buf1 -> buf1.readCollection(Sets::newHashSetWithExpectedSize, buf2 -> ModRegistries.QUESTS.get()
                                                                                                                                                                                                                                                                                                        .getValue(buf2.readResourceLocation())));
        Map<ProgressionQuest, QuestProgress> questProgress = buf.readMap(buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()), QuestProgress::fromNetwork);

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), questsByStatus, questProgress);
    }


    public @NotNull CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("player", this.playerData.get().getUUID());
        tag.put("questsByStatus", CollectionHelper.saveToNBT(this.questsByStatus, Enum::name, quests -> CollectionHelper.saveToNBT(quests, quest -> StringTag.valueOf(quest.getId().toString()))));
        tag.put("questProgress", CollectionHelper.saveToNBT(this.questProgress, quest -> quest.getId().toString(), QuestProgress::saveToCompoundTag));

        return tag;
    }


    public void saveToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeMap(this.questsByStatus, FriendlyByteBuf::writeEnum, (buf1, quests) -> buf1.writeCollection(quests, (buf2, quest) -> buf2.writeResourceLocation(quest.getId())));
        buf.writeMap(this.questProgress, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()), (buf1, progress) -> progress.toNetwork(buf1));
    }


    public void forEachCompleted(Consumer<ProgressionQuest> questConsumer) {
        this.getCompletedQuests().forEach(questConsumer);
    }


    public ImmutableSet<ProgressionQuest> getCompletedQuests() {
        return this.getQuestsByStatus(ProgressionQuest.Status.COMPLETE);
    }


    public QuestProgress getOrStartProgress(ProgressionQuest quest) {
        QuestProgress questProgress = this.questProgress.get(quest);
        if(questProgress == null) {
            questProgress = new QuestProgress(quest);
            this.startProgress(quest, questProgress);
        }
        return questProgress;
    }


    public ImmutableSet<ProgressionQuest> getQuestsByStatus(ProgressionQuest.Status questStatus) {
        return ImmutableSet.copyOf(this.questsByStatus.get(questStatus));
    }


    public void forEachActive(Consumer<ProgressionQuest> questConsumer) {
        this.getActiveQuests().forEach(questConsumer);
    }


    public ImmutableSet<ProgressionQuest> getActiveQuests() {
        return this.getQuestsByStatus(ProgressionQuest.Status.ACTIVE);
    }


    private void startProgress(ProgressionQuest quest, QuestProgress questProgress) {
        questProgress.updateProgress(this.playerData.get());
        this.questProgress.putIfAbsent(quest, questProgress);
    }


    public void updateQuestStatus(QuestManager questManager) { //TODO: Check for some quests if parent is complete!
        questManager.getAllQuestsForLevel(this.playerData.get().getCurrentLevel()).forEach(quest -> {
            QuestProgress questProgress = this.getOrStartProgress(quest);
            ProgressionQuest.Status previousStatus = questProgress.getCurrentQuestStatus();
            questProgress.updateProgress(this.playerData.get());
            ProgressionQuest.Status currentStatus = questProgress.getCurrentQuestStatus();
            if(previousStatus != currentStatus) {
                this.questsByStatus.compute(previousStatus, (status, progressionQuests) -> (progressionQuests == null ? new HashSet<>() : progressionQuests)).remove(quest);
                this.questsByStatus.compute(questProgress.getCurrentQuestStatus(), (status, progressionQuests) -> (progressionQuests == null ? new HashSet<>() : progressionQuests)).add(quest);
            }
        });
    }


    public void copy(PlayerQuests playerQuests) {
        if(this.equals(playerQuests)) {
            return;
        }

        this.questsByStatus.clear();
        this.questsByStatus.putAll(playerQuests.questsByStatus);
        this.questProgress.clear();
        this.questProgress.putAll(playerQuests.questProgress);
    }


    public void stopListening() {
        this.questProgress.forEach((quest, progress) -> progress.stopListening(playerData.get()));
    }
}
