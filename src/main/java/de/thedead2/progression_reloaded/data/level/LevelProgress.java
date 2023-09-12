package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.api.progress.IProgressInfo;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;


/**
 * Progress of a level is dependent on the player or the team. Different players or teams can have different progress of a level.
 **/
public class LevelProgress implements IProgressInfo {

    private final ProgressionLevel level;

    private final KnownPlayer player;

    private boolean rewarded;


    public LevelProgress(ProgressionLevel level, KnownPlayer player) {
        this(level, player, false);
    }


    public LevelProgress(ProgressionLevel level, KnownPlayer player, boolean rewarded) {
        this.level = level;
        this.player = player;
        this.rewarded = rewarded;
    }


    public static LevelProgress loadFromCompoundTag(CompoundTag tag) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("level")));
        KnownPlayer player1 = KnownPlayer.fromCompoundTag(tag.getCompound("player"));
        boolean rewarded = tag.getBoolean("rewarded");
        return new LevelProgress(level1, player1, rewarded);
    }


    public static LevelProgress fromNetwork(FriendlyByteBuf buf) {
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());
        KnownPlayer player = KnownPlayer.fromNetwork(buf);
        boolean rewarded = buf.readBoolean();

        return new LevelProgress(level, player, rewarded);
    }


    @Override
    public float getPercent() {
        Collection<ProgressionQuest> levelQuests = LevelManager.getInstance().getQuestManager().getMainQuestsForLevel(this.level);
        if(levelQuests.isEmpty()) {
            return 0.0F;
        }
        else {
            float quests = (float) levelQuests.size();
            float completedQuests = this.countCompletedQuestCriteria();
            return completedQuests / quests;
        }
    }


    /**
     * Returns true if all main quests of the level have been completed by the given player
     **/
    @Override
    public boolean isDone() {
        boolean flag = false;
        for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, this.player)) {
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
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.level.getId());
        this.player.toNetwork(buf);
        buf.writeBoolean(this.rewarded);
    }


    @Override
    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("level", this.level.getId().toString());
        tag.put("player", this.player.toCompoundTag());
        tag.putBoolean("rewarded", this.rewarded);
        return tag;
    }


    @Override
    public void reset() {
        this.level.getQuests().forEach(id -> LevelManager.getInstance().getQuestManager().revoke(id, this.player));
        this.rewarded = false;
    }


    @Override
    public void complete() {
        this.level.getQuests().forEach(id -> LevelManager.getInstance().getQuestManager().award(id, this.player));
    }


    private float countCompletedQuestCriteria() {
        float i = 0f;

        for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, this.player)) {
            if(questProgress != null) {
                i += questProgress.getPercent();
            }
        }

        return i;
    }


    public boolean hasBeenRewarded() {
        return this.rewarded;
    }


    public void setRewarded(boolean value) {
        this.rewarded = value;
    }
}
