package de.thedead2.progression_reloaded.data.level;

import com.google.gson.*;
import de.thedead2.progression_reloaded.util.registries.ModRegistriesDynamicSerializer;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.rewards.RewardStrategy;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;

public class ProgressionLevel implements ModRegistriesDynamicSerializer {
    private final int index;
    private final String name;
    private final ResourceLocation id;
    private final Set<IReward> rewards;
    private final RewardStrategy rewardStrategy;
    @Nullable private final ResourceLocation previousLevel;
    @Nullable private final ResourceLocation nextLevel;
    private final Collection<ResourceLocation> quests;

    public ProgressionLevel(int index, String name, ResourceLocation id, RewardStrategy rewardStrategy, Collection<ResourceLocation> quests, Set<IReward> rewards, @Nullable ResourceLocation previousLevel, @Nullable ResourceLocation nextLevel) {
        this.index = index;
        this.name = name;
        this.id = id;
        this.rewardStrategy = rewardStrategy;
        this.quests = quests;
        this.previousLevel = previousLevel;
        this.nextLevel = nextLevel;
        this.rewards = rewards;
    }

    public static ProgressionLevel fromKey(ResourceLocation level) {
        return ModRegistries.LEVELS.get().getValue(level);
    }

    public static ProgressionLevel fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int index = jsonObject.get("index").getAsInt();
        String name = jsonObject.get("name").getAsString();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        RewardStrategy strategy = RewardStrategy.valueOf(jsonObject.get("rewards_strategy").getAsString());
        ResourceLocation previous = null, next = null;
        if(jsonObject.has("previous")) previous = new ResourceLocation(jsonObject.get("previous").getAsString());
        if(jsonObject.has("next")) next = new ResourceLocation(jsonObject.get("next").getAsString());
        JsonArray quests = jsonObject.get("quests").getAsJsonArray();
        Set<ResourceLocation> levelQuests = new HashSet<>();
        quests.forEach(jsonElement1 -> levelQuests.add(new ResourceLocation(jsonElement1.getAsString())));
        JsonArray rewards = jsonObject.get("rewards").getAsJsonArray();
        Set<IReward> levelRewards = new HashSet<>();
        rewards.forEach(jsonElement1 -> levelRewards.add(IReward.createFromJson(jsonElement1.getAsJsonObject())));

        return new ProgressionLevel(index, name, id, strategy, levelQuests, levelRewards, previous, next);
    }

    public JsonObject toJson(){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOGGER.debug("Level {}: \n{}", this.name, gson.toJson(this));*/
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("index", this.index);
        jsonObject.addProperty("name", this.name);
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.addProperty("rewards_strategy", this.rewardStrategy.toString());
        if(this.previousLevel != null) jsonObject.addProperty("previous", this.previousLevel.toString());
        if(this.nextLevel != null) jsonObject.addProperty("next", this.nextLevel.toString());
        JsonArray quests = new JsonArray();
        this.quests.forEach(resourceLocation -> quests.add(resourceLocation.toString()));
        jsonObject.add("quests", quests);
        JsonArray rewards = new JsonArray();
        this.rewards.forEach(reward -> rewards.add(reward.saveToJson()));
        jsonObject.add("rewards", rewards);

        return jsonObject;
    }

    public boolean contains(ProgressionLevel other) {
        ProgressionLevel previousLevel = ModRegistries.LEVELS.get().getValue(this.previousLevel);
        if(this.equals(other) || (previousLevel != null && previousLevel.equals(other))) return true;
        else if(previousLevel == null) return false;
        else return previousLevel.contains(other);
    }

    public boolean contains(ProgressionQuest quest) {
        ProgressionLevel previousLevel = ModRegistries.LEVELS.get().getValue(this.previousLevel);
        return this.quests.contains(quest.getId()) || (previousLevel != null && previousLevel.contains(quest));
    }

    public @Nullable ResourceLocation getPreviousLevel() {
        return previousLevel;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void rewardPlayer(SinglePlayer player) {
        this.rewardStrategy.reward(this.rewards, player);
    }

    public Collection<ResourceLocation> getQuests() {
        return this.quests;
    }

    public @Nullable ResourceLocation getNextLevel(){
        return this.nextLevel;
    }
}
