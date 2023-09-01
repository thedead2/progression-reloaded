package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Progress of a level is dependent on the player or the team. Different players or teams can have different progress of a level.
 **/
public class LevelProgress {

    private final ProgressionLevel level;

    private final Map<KnownPlayer, Boolean> rewarded;


    public LevelProgress(ProgressionLevel level) {
        this(level, new HashMap<>());
    }


    public LevelProgress(ProgressionLevel level, Map<KnownPlayer, Boolean> rewarded) {
        this.level = level;
        this.rewarded = rewarded;
    }


    public static LevelProgress loadFromCompoundTag(CompoundTag tag) {
        ProgressionLevel level1 = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("level")));
        CompoundTag tag1 = tag.getCompound("progress");
        Map<KnownPlayer, Boolean> map = new HashMap<>();
        tag1.getAllKeys().stream().filter(s -> s.contains("-player")).forEach(s -> {
            KnownPlayer player = KnownPlayer.fromCompoundTag(tag1.getCompound(s));
            boolean rewarded = tag1.getBoolean(player.id() + "-rewarded");
            map.put(player, rewarded);
        });
        return new LevelProgress(level1, map);
    }


    /**
     * Returns true if all main quests of the level have been completed by the given player
     **/
    public boolean isDone(KnownPlayer player) {
        boolean flag = false;
        for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, player)) {
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


    public float getPercent(KnownPlayer player) {
        Collection<ProgressionQuest> levelQuests = LevelManager.getInstance().getQuestManager().getMainQuestsForLevel(this.level);
        if(levelQuests.isEmpty()) {
            return 0.0F;
        }
        else {
            float quests = (float) levelQuests.size();
            float completedQuests = this.countCompletedCriteria(player);
            return completedQuests / quests;
        }
    }


    private float countCompletedCriteria(KnownPlayer player) {
        float i = 0f;

        for(QuestProgress questProgress : LevelManager.getInstance().getQuestManager().getMainQuestProgress(this.level, player)) {
            if(questProgress != null && questProgress.isDone()) {
                i++;
            }
        }

        return i;
    }


    public boolean hasBeenRewarded(KnownPlayer player) {
        return this.rewarded.get(player) != null ? this.rewarded.get(player) : false;
    }


    public void setRewarded(KnownPlayer player, boolean value) {
        this.rewarded.put(player, value);
    }


    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("level", this.level.getId().toString());

        CompoundTag progressTag = new CompoundTag();
        this.rewarded.forEach((knownPlayer, aBoolean) -> {
            progressTag.put(knownPlayer.id() + "-player", knownPlayer.toCompoundTag());
            progressTag.putBoolean(knownPlayer.id() + "-rewarded", aBoolean);
        });
        tag.put("progress", progressTag);
        return tag;
    }
}
