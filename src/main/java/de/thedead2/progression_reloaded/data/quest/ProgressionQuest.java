package de.thedead2.progression_reloaded.data.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.QuestManager;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;


public class ProgressionQuest implements IProgressable<ProgressionQuest> {

    private final QuestDisplayInfo displayInfo;

    private final QuestTasks tasks;


    public ProgressionQuest(QuestDisplayInfo displayInfo, QuestTasks tasks) {
        this.displayInfo = displayInfo;
        this.tasks = tasks;
    }


    public static ProgressionQuest fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        QuestDisplayInfo displayInfo = QuestDisplayInfo.fromJson(jsonObject.get("display"));
        QuestTasks tasks = QuestTasks.fromJson(jsonObject.getAsJsonObject("tasks"));

        return new ProgressionQuest(displayInfo, tasks);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("display", this.displayInfo.toJson());
        jsonObject.add("tasks", this.tasks.toJson());

        return jsonObject;
    }


    public boolean isMainQuest() {
        return this.displayInfo.mainQuest();
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


    public ResourceLocation getId() {
        return this.displayInfo.id();
    }


    @Nullable
    public ResourceLocation getParent() {
        return this.displayInfo.parentQuest();
    }


    public boolean isParentDone(QuestManager questManager, PlayerData player) {
        return questManager.isParentDone(this, player);
    }


    public QuestTasks getTasks() {
        return tasks;
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
        return this.displayInfo.title();
    }


    public QuestDisplayInfo getDisplay() {
        return this.displayInfo;
    }


    public boolean isDone(PlayerData player) {
        return player.getQuestData().getOrStartProgress(this).isDone();
    }


    public boolean isActive(PlayerData player) {
        return player.getQuestData().getStartedOrActiveQuests().contains(this);
    }

}
