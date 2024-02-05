package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * Progress of a level is dependent on the player or the team. Different players or teams can have different progress of a level.
 **/
public class LevelProgress implements IProgressInfo<ProgressionLevel> {

    private final Supplier<PlayerData> player;
    private final ProgressionLevel level;

    private boolean rewarded;


    public LevelProgress(Supplier<PlayerData> player, ProgressionLevel level) {
        this(player, level, false);
    }


    public LevelProgress(Supplier<PlayerData> player, ProgressionLevel level, boolean rewarded) {
        this.player = player;
        this.level = level;
        this.rewarded = rewarded;
    }


    public static LevelProgress fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("level")));
        boolean rewarded = tag.getBoolean("rewarded");

        return new LevelProgress(() -> PlayerDataManager.getPlayerData(uuid), level1, rewarded);
    }


    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", this.player.get().getUUID());
        tag.putString("level", this.level.getId().toString());
        tag.putBoolean("rewarded", this.rewarded);

        return tag;
    }


    public static LevelProgress fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());
        boolean rewarded = buf.readBoolean();

        return new LevelProgress(() -> PlayerDataManager.getPlayerData(uuid), level, rewarded);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.player.get().getUUID());
        buf.writeResourceLocation(this.level.getId());
        buf.writeBoolean(this.rewarded);
    }


    @Override
    public float getPercent() {
        Collection<ProgressionQuest> levelQuests = LevelManager.getInstance().getQuestManager().getMainQuestsForLevel(this.level);
        if(levelQuests.isEmpty()) {
            return 1F;
        }
        else {
            float completedQuestsPercent = 0f;
            //FIXME: When only on client there is no QuestManager or LevelManager for accessing
            for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, this.player.get())) {
                if(questProgress != null) {
                    completedQuestsPercent += questProgress.getPercent();
                }
            }

            return completedQuestsPercent / levelQuests.size();
        }
    }


    /**
     * Returns true if all main quests of the level have been completed by the given player
     **/
    @Override
    public boolean isDone() {
        boolean flag = false;
        for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, this.player.get())) {
            if(questProgress != null && questProgress.isDone()) {
                flag = true;
            }
            else {
                flag = false;
                break;
            }
        }

        return flag;
    }


    @Override
    public void reset() {
        this.level.getQuests().forEach(id -> LevelManager.getInstance().getQuestManager().revoke(id, this.player.get()));
        this.rewarded = false;
    }


    @Override
    public void complete() {
        this.level.getQuests().forEach(id -> LevelManager.getInstance().getQuestManager().award(id, true, this.player.get()));
    }


    @Override
    public ProgressionLevel getProgressable() {
        return this.level;
    }


    public boolean hasBeenRewarded() {
        return this.rewarded;
    }


    public void setRewarded(boolean value) {
        this.rewarded = value;
    }
}
