package de.thedead2.progression_reloaded.data.level;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class ProgressionLevel implements IProgressable<ProgressionLevel> {

    private final LevelDisplayInfo displayInfo;

    private final Rewards rewards;

    private final Collection<ResourceLocation> quests;


    public ProgressionLevel(LevelDisplayInfo displayInfo, Rewards rewards, Collection<ResourceLocation> quests) {
        this.displayInfo = displayInfo;
        this.rewards = rewards;
        this.quests = quests;
    }


    public static ProgressionLevel fromKey(ResourceLocation level) {
        return ModRegistries.LEVELS.get().getValue(level);
    }


    public static ProgressionLevel fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        LevelDisplayInfo displayInfo = LevelDisplayInfo.fromJson(jsonObject.get("display"));

        JsonArray quests = jsonObject.get("quests").getAsJsonArray();
        Set<ResourceLocation> levelQuests = new HashSet<>();
        quests.forEach(jsonElement1 -> levelQuests.add(new ResourceLocation(jsonElement1.getAsString())));

        Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));

        return new ProgressionLevel(displayInfo, rewards, levelQuests);
    }


    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("display", this.displayInfo.toJson());

        JsonArray quests = new JsonArray();
        this.quests.forEach(resourceLocation -> quests.add(resourceLocation.toString()));
        jsonObject.add("quests", quests);

        jsonObject.add("rewards", this.rewards.toJson());

        return jsonObject;
    }


    public boolean contains(ProgressionLevel other) {
        if(this.equals(LevelManager.CREATIVE)) {
            return true;
        }

        ProgressionLevel previousLevel = this.getParent() != null ? ModRegistries.LEVELS.get().getValue(this.getParent()) : null;
        if(this.equals(other) || (previousLevel != null && previousLevel.equals(other))) {
            return true;
        }
        else if(previousLevel == null) {
            return false;
        }
        else {
            return previousLevel.contains(other);
        }
    }


    @Override
    public int compareTo(@NotNull ProgressionLevel other) {
        //Cases:
        // 1.) other parent of this --> done
        // 2.) this parent of other --> done
        // 3.) this equals other --> done
        // 4.) neither of the above --> we can't compare them!
        ResourceLocation thisId = this.getId();
        ResourceLocation otherId = other.getId();

        ResourceLocation thisPrevious = this.getParent();
        ResourceLocation otherPrevious = other.getParent();

        if(thisId.equals(otherId)) { // same levels
            return 0;
        }
        else if(thisPrevious != null && thisPrevious.equals(otherId)) {
            return 1; // this is greater
        }
        else if(otherPrevious != null && otherPrevious.equals(thisId)) {
            return -1; // this is less
        }
        else {
            throw new IllegalArgumentException("Can't compare level " + thisId + " with level " + otherId + " as they are not related to each other!");
        }
    }


    @Nullable
    public ResourceLocation getParent() {
        return this.displayInfo.previousLevel();
    }


    public ResourceLocation getId() {
        return this.displayInfo.getId();
    }


    public Component getTitle() {
        return this.displayInfo.getTitle();
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public ImmutableSet<ResourceLocation> getQuests() {
        return ImmutableSet.copyOf(this.quests);
    }


    public LevelDisplayInfo getDisplay() {
        return this.displayInfo;
    }


    public Rewards getRewards() {
        return this.rewards;
    }


    public boolean contains(ProgressionQuest quest) {
        ProgressionLevel previousLevel = this.getParent() != null ? ModRegistries.LEVELS.get().getValue(this.getParent()) : null;
        return this.quests.contains(quest.getId()) || (previousLevel != null && previousLevel.contains(quest));
    }
}
