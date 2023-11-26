package de.thedead2.progression_reloaded.data.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.QuestManager;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;


public class ProgressionQuest implements IProgressable<ProgressionQuest> {

    private final QuestDisplayInfo displayInfo;

    private final Rewards rewards;

    private final QuestCriteria criteria;


    public ProgressionQuest(QuestDisplayInfo displayInfo, Rewards rewards, QuestCriteria criteria) {
        this.displayInfo = displayInfo;
        this.rewards = rewards;
        this.criteria = criteria;
    }


    public static ProgressionQuest fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        QuestDisplayInfo displayInfo = QuestDisplayInfo.fromJson(jsonObject.get("display"));
        QuestCriteria criteria = QuestCriteria.fromJson(jsonObject.get("criteria"));
        Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));

        return new ProgressionQuest(displayInfo, rewards, criteria);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("display", this.displayInfo.toJson());
        jsonObject.add("criteria", this.criteria.toJson());
        jsonObject.add("rewards", this.rewards.toJson());

        return jsonObject;
    }


    public boolean isMainQuest() {
        return this.displayInfo.mainQuest();
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public boolean hasDirectParent() {
        return this.displayInfo.parentQuest() != null;
    }


    @Override
    public int compareTo(@NotNull ProgressionQuest other) {
        ResourceLocation thisId = this.getId();
        ResourceLocation otherId = other.getId();

        ResourceLocation thisPrevious = this.getParent();
        ResourceLocation otherPrevious = other.getParent();

        ProgressionLevel thisLevel = LevelManager.getInstance().getLevelForQuest(this);
        ProgressionLevel otherLevel = LevelManager.getInstance().getLevelForQuest(other);

        if(thisLevel.equals(otherLevel)) {
            if(thisId.equals(otherId)) { // same quests
                return 0;
            }
            else if(thisPrevious != null && thisPrevious.equals(otherId)) {
                return 1; // this is greater
            }
            else if(otherPrevious != null && otherPrevious.equals(thisId)) {
                return -1; // this is less
            }
            else {
                throw new IllegalArgumentException("Can't compare quest " + thisId + " with quest " + otherId + " as they are not related to each other!");
            }
        }
        else {
            return thisLevel.compareTo(otherLevel);
        }
    }


    public Map<String, SimpleTrigger<?>> getCriteria() {
        return this.criteria.getCriteria();
    }


    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteria.getCriteriaStrategy();
    }


    @Nullable
    public ResourceLocation getParent() {
        return this.displayInfo.parentQuest();
    }


    public boolean isParentDone(QuestManager questManager, PlayerData player) {
        return questManager.isParentDone(this, player);
    }


    public ResourceLocation getId() {
        return this.displayInfo.id();
    }


    public boolean equalsAny(Collection<ProgressionQuest> quests) {
        boolean flag = false;
        for(ProgressionQuest quest : quests) {
            flag = this.equals(quest);

            if(flag) {
                break;
            }
        }
        return flag;
    }


    public boolean hasChild() {
        return LevelManager.getInstance().getQuestManager().hasChild(this);
    }


    public Component getTitle() {
        return this.displayInfo.getTitle();
    }


    public QuestDisplayInfo getDisplay() {
        return this.displayInfo;
    }


    public Rewards getRewards() {
        return this.rewards;
    }


    public boolean isDone(PlayerData player) {
        return player.getQuestData().getOrStartProgress(this).isDone();
    }
}
