package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.api.INbtSerializable;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
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
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.misc.TimeKeeper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.isRunningOnServerThread;


public class PlayerQuests implements INbtSerializable, INetworkSerializable {

    private final TimeKeeper timeKeeper;
    private final Supplier<PlayerData> playerData;

    @Nullable
    private ProgressionQuest followedQuest;

    private final EnumMap<QuestStatus, Set<ProgressionQuest>> questsByStatus = Maps.newEnumMap(QuestStatus.class);

    private final Map<ProgressionQuest, QuestProgress> questProgress = Maps.newHashMap();


    public PlayerQuests(Supplier<PlayerData> playerData) {
        this(playerData, null, Maps.newHashMap(), Maps.newHashMap());
    }


    private PlayerQuests(Supplier<PlayerData> playerData, @Nullable ProgressionQuest followedQuest, Map<ProgressionQuest, QuestProgress> questProgress, Map<QuestStatus, Set<ProgressionQuest>> questsByStatus) {
        this.timeKeeper = new TimeKeeper(() -> this, playerData.get() != null ? playerData.get().getUUID() : UUID.randomUUID());
        this.playerData = playerData;
        this.followedQuest = followedQuest;
        this.questProgress.putAll(questProgress);
        this.questsByStatus.putAll(questsByStatus);
    }


    public static PlayerQuests fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionQuest followedQuest = SerializationHelper.getNullable(tag, "followedQuest", tag1 -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag1.getAsString())));
        Map<ProgressionQuest, QuestProgress> questProgress = CollectionHelper.loadFromNBT(tag.getCompound("questProgress"), s -> ModRegistries.QUESTS.get().getValue(new ResourceLocation(s)), tag1 -> QuestProgress.fromNBT((CompoundTag) tag1));
        Map<QuestStatus, Set<ProgressionQuest>> questsByStatus = CollectionHelper.loadFromNBT(tag.getCompound("questsByStatus"), QuestStatus::valueOf, tag1 -> CollectionHelper.loadFromNBT(Sets::newHashSetWithExpectedSize, (ListTag) tag1, tag2 -> ModRegistries.QUESTS.get()
                                                                                                                                                                                                                                                                          .getValue(new ResourceLocation(tag2.getAsString()))));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), followedQuest, questProgress, questsByStatus);
    }


    public static PlayerQuests fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionQuest followedQuest = buf.readNullable(buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()));
        Map<ProgressionQuest, QuestProgress> questProgress = buf.readMap(buf1 -> ModRegistries.QUESTS.get().getValue(buf1.readResourceLocation()), QuestProgress::fromNetwork);
        Map<QuestStatus, Set<ProgressionQuest>> questsByStatus = buf.readMap(buf1 -> buf1.readEnum(QuestStatus.class), buf1 -> buf1.readCollection(Sets::newHashSetWithExpectedSize, buf2 -> ModRegistries.QUESTS.get().getValue(buf2.readResourceLocation())));

        return new PlayerQuests(() -> PlayerDataManager.getPlayerData(uuid), followedQuest, questProgress, questsByStatus);
    }


    public TimeKeeper getTimeKeeper() {
        return timeKeeper;
    }


    @Override
    public @NotNull CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("player", this.playerData.get().getUUID());
        SerializationHelper.addNullable(this.followedQuest, tag, "followedQuest", quest -> StringTag.valueOf(quest.getId().toString()));
        tag.put("questProgress", CollectionHelper.saveToNBT(this.questProgress, quest -> quest.getId().toString(), QuestProgress::toNBT));
        tag.put("questsByStatus", CollectionHelper.saveToNBT(this.questsByStatus, Enum::name, quests -> CollectionHelper.saveToNBT(quests, quest -> StringTag.valueOf(quest.getId().toString()))));

        return tag;
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeNullable(this.followedQuest, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()));
        buf.writeMap(this.questProgress, (buf1, quest) -> buf1.writeResourceLocation(quest.getId()), (buf1, progress) -> progress.toNetwork(buf1));
        buf.writeMap(this.questsByStatus, FriendlyByteBuf::writeEnum, (buf1, quests) -> buf1.writeCollection(quests, (buf2, quest) -> buf2.writeResourceLocation(quest.getId())));
    }


    public QuestProgress getOrStartProgress(ResourceLocation questId) {
        return this.getOrStartProgress(ModRegistries.QUESTS.get().getValue(questId));
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

        if(newStatus == QuestStatus.COMPLETE || newStatus == QuestStatus.FAILED) {
            this.timeKeeper.stopListeningForQuest(quest.getId());
        }

        PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(this.playerData.get()), this.playerData.get().getServerPlayer());

        PREventFactory.onQuestStatusChanged(quest, oldStatus, newStatus, this.playerData.get());
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


    @Nullable
    public ProgressionQuest getFollowedQuest() {
        return followedQuest;
    }


    public QuestProgress getFollowedQuestProgress() {
        return this.getOrStartProgress(this.followedQuest);
    }


    public QuestProgress getOrStartProgress(ProgressionQuest quest) {
        QuestProgress questProgress = this.questProgress.get(quest);
        if(questProgress == null && quest != null) {
            questProgress = new QuestProgress(quest, this.playerData);
            this.startProgress(quest, questProgress);
        }
        return questProgress;
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


    public boolean followQuest(ResourceLocation questId) {
        return this.followQuest(ModRegistries.QUESTS.get().getValue(questId));
    }


    public boolean followQuest(ProgressionQuest quest) {
        boolean flag = this.followedQuest != null && this.followedQuest.equals(quest);
        if(!flag) {
            this.followedQuest = quest;

            PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(this.playerData.get()), this.playerData.get().getServerPlayer());
        }
        return flag;
    }


    public void stopTimeKeeping() {
        this.timeKeeper.stopGracefully();
    }
}
