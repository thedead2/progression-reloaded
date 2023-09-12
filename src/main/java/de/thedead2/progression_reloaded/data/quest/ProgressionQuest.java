package de.thedead2.progression_reloaded.data.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.QuestManager;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.registries.ModRegistriesDynamicSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;


public class ProgressionQuest implements ModRegistriesDynamicSerializer, Comparable<ProgressionQuest> {

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


    public ResourceLocation getId() {
        return this.displayInfo.getId();
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public boolean isMainQuest() {
        return this.displayInfo.isMainQuest();
    }


    public ResourceLocation getParentQuest() {
        return this.displayInfo.getParentQuest();
    }


    public Map<String, SimpleTrigger<?>> getCriteria() {
        return this.criteria.getCriteria();
    }


    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteria.getCriteriaStrategy();
    }


    public boolean isParentDone(QuestManager questManager, KnownPlayer player) {
        return questManager.isParentDone(this, player);
    }


    public boolean isDone(QuestManager questManager, KnownPlayer player) {
        return questManager.getOrStartProgress(this, player).isDone();
    }


    public boolean hasDirectParent() {
        return this.displayInfo.getParentQuest() != null;
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


    @Override
    public int compareTo(@NotNull ProgressionQuest o) {
        ResourceLocation thisId = this.getId();
        ResourceLocation otherId = o.getId();

        ResourceLocation thisPrevious = this.getParentQuest();
        ResourceLocation otherPrevious = o.getParentQuest();

        if(otherId.equals(thisId) || (otherPrevious == null && thisPrevious == null)) {
            return 0;
        }
        else if(otherPrevious != null && otherPrevious.equals(thisId)) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
