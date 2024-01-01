package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.isRunningOnServerThread;


public class PlayerQuests {

    private final Supplier<PlayerData> playerData;

    private final EnumMap<QuestStatus, Set<ProgressionQuest>> questsByStatus = Maps.newEnumMap(QuestStatus.class);

    private final Map<ProgressionQuest, QuestProgress> questProgress = Maps.newHashMap();


    public PlayerQuests(Supplier<PlayerData> playerData) {
        this(playerData, Maps.newHashMap(), Maps.newHashMap());
    }


    private PlayerQuests(Supplier<PlayerData> playerData, Map<ProgressionQuest, QuestProgress> questProgress, Map<QuestStatus, Set<ProgressionQuest>> questsByStatus) {
        this.playerData = playerData;
        this.questProgress.putAll(questProgress);
        this.questsByStatus.putAll(questsByStatus);
    }


    public static PlayerQuests loadFromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        Map<ProgressionQuest, QuestProgress> questProgress = CollectionHelper.loadFromNBT(tag.getCompound("questProgress"), s -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(s)), tag1 -> QuestProgress.fromNBT((CompoundTag) tag1));
        Map<QuestStatus, Set<ProgressionQuest>> questsByStatus = CollectionHelper.loadFromNBT(tag.getCompound("questsByStatus"), QuestStatus::valueOf, tag1 -> CollectionHelper.loadFromNBT(Sets::newHashSetWithExpectedSize, (ListTag) tag1, tag2 -> ModRegistries.QUESTS.get()
                                                                                                                                                                                                                                                                          .getValue(new ResourceLocation(tag2.getAsString()))));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), questProgress, questsByStatus);
    }


    public static PlayerQuests loadFromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        Map<ProgressionQuest, QuestProgress> questProgress = buf.readMap(buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()), QuestProgress::fromNetwork);
        Map<QuestStatus, Set<ProgressionQuest>> questsByStatus = buf.readMap(buf1 -> buf1.readEnum(QuestStatus.class), buf1 -> buf1.readCollection(Sets::newHashSetWithExpectedSize, buf2 -> ModRegistries.QUESTS.get().getValue(buf2.readResourceLocation())));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), questProgress, questsByStatus);
    }


    public @NotNull CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("player", this.playerData.get().getUUID());
        tag.put("questProgress", CollectionHelper.saveToNBT(this.questProgress, quest -> quest.getId().toString(), QuestProgress::saveToCompoundTag));
        tag.put("questsByStatus", CollectionHelper.saveToNBT(this.questsByStatus, Enum::name, quests -> CollectionHelper.saveToNBT(quests, quest -> StringTag.valueOf(quest.getId().toString()))));

        return tag;
    }


    public void saveToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeMap(this.questProgress, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()), (buf1, progress) -> progress.toNetwork(buf1));
        buf.writeMap(this.questsByStatus, FriendlyByteBuf::writeEnum, (buf1, quests) -> buf1.writeCollection(quests, (buf2, quest) -> buf2.writeResourceLocation(quest.getId())));
    }


    public QuestProgress getOrStartProgress(ResourceLocation questId) {
        return this.getOrStartProgress(ModRegistries.QUESTS.get().getValue(questId));
    }

    public QuestProgress getOrStartProgress(ProgressionQuest quest) {
        QuestProgress questProgress = this.questProgress.get(quest);
        if(questProgress == null) {
            questProgress = new QuestProgress(quest, this.playerData);
            this.startProgress(quest, questProgress);
        }
        return questProgress;
    }


    private void startProgress(ProgressionQuest quest, QuestProgress questProgress) {
        if(isRunningOnServerThread()) {
            questProgress.startListening();
        }
        this.questProgress.putIfAbsent(quest, questProgress);
    }


    public ImmutableSet<ProgressionQuest> getQuestsByStatus(QuestStatus questStatus) {
        Set<ProgressionQuest> quests = this.questsByStatus.get(questStatus);
        return quests != null ? ImmutableSet.copyOf(quests) : ImmutableSet.of();
    }


    public ImmutableSet<ProgressionQuest> getStartedOrActiveQuests() {
        Set<ProgressionQuest> quests = new HashSet<>();
        quests.addAll(this.getQuestsByStatus(QuestStatus.STARTED));
        quests.addAll(this.getQuestsByStatus(QuestStatus.ACTIVE));

        return ImmutableSet.copyOf(quests);
    }


    public ImmutableSet<ProgressionQuest> getFinishedQuests() {
        Set<ProgressionQuest> quests = new HashSet<>();
        quests.addAll(this.getQuestsByStatus(QuestStatus.COMPLETE));
        quests.addAll(this.getQuestsByStatus(QuestStatus.FAILED));

        return ImmutableSet.copyOf(quests);
    }


    public void updateQuestStatus(ProgressionQuest quest) {
        QuestProgress questProgress = this.getOrStartProgress(quest);
        QuestStatus currentStatus = questProgress.getCurrentQuestStatus();
        this.questsByStatus.compute(currentStatus, (status, progressionQuests) -> (progressionQuests == null ? new HashSet<>() : progressionQuests)).add(quest);
    }


    public void onQuestStatusChanged(ProgressionQuest quest, QuestStatus oldStatus, QuestStatus newStatus) {
        ModHelper.LOGGER.debug("Quest status for quest {} changed from {} to {} for player {}", quest.getId(), oldStatus, newStatus, this.playerData.get().getName());
        if(oldStatus != null) {
            this.questsByStatus.compute(oldStatus, (status, progressionQuests) -> (progressionQuests == null ? new HashSet<>() : progressionQuests)).remove(quest);
        }
        else {
            throw new IllegalArgumentException("old status null");
        }
        if(newStatus != null) {
            this.questsByStatus.compute(newStatus, (status, progressionQuests) -> (progressionQuests == null ? new HashSet<>() : progressionQuests)).add(quest);
        }
        else {
            throw new IllegalArgumentException("new status null");
        }

        PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(this.playerData.get()), this.playerData.get().getServerPlayer());

        PREventFactory.onQuestStatusChanged(quest, oldStatus, newStatus, this.playerData.get());
    }


    public void copy(PlayerQuests playerQuests) {
        if(this.equals(playerQuests)) {
            this.startListening();
            return;
        }

        this.questsByStatus.clear();
        this.questsByStatus.putAll(playerQuests.questsByStatus);
        this.questProgress.clear();
        this.questProgress.putAll(playerQuests.questProgress);

        this.startListening();
    }


    public void startListening() {
        if(isRunningOnServerThread()) {
            this.questProgress.forEach((quest, progress) -> progress.startListening());
        }
    }


    public void stopListening() {
        if(isRunningOnServerThread()) {
            this.questProgress.forEach((quest, progress) -> progress.stopListening());
        }
    }
}
