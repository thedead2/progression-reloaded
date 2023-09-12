package de.thedead2.progression_reloaded.data.level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import de.thedead2.progression_reloaded.util.registries.ModRegistriesDynamicSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class ProgressionLevel implements ModRegistriesDynamicSerializer, Comparable<ProgressionLevel> {

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


    public ResourceLocation getId() {
        return this.displayInfo.getId();
    }


    public boolean contains(ProgressionLevel other) {
        if(this.equals(TestLevels.CREATIVE)) {
            return true;
        }

        ProgressionLevel previousLevel = this.getPreviousLevel() != null ? ModRegistries.LEVELS.get().getValue(this.getPreviousLevel()) : null;
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


    public boolean contains(ProgressionQuest quest) {
        ProgressionLevel previousLevel = this.getPreviousLevel() != null ? ModRegistries.LEVELS.get().getValue(this.getPreviousLevel()) : null;
        return this.quests.contains(quest.getId()) || (previousLevel != null && previousLevel.contains(quest));
    }


    public @Nullable ResourceLocation getPreviousLevel() {
        return this.displayInfo.getPreviousLevel();
    }


    public Component getTitle() {
        return this.displayInfo.getTitle();
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public Collection<ResourceLocation> getQuests() {
        return this.quests;
    }


    public LevelDisplayInfo getDisplay() {
        return this.displayInfo;
    }


    public Rewards getRewards() {
        return this.rewards;
    }


    @Override
    public int compareTo(@NotNull ProgressionLevel o) {
        ResourceLocation thisId = this.getId();
        ResourceLocation otherId = o.getId();

        ResourceLocation thisPrevious = this.getPreviousLevel();
        ResourceLocation otherPrevious = o.getPreviousLevel();

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
