package de.thedead2.progression_reloaded.data.level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.registries.ModRegistriesDynamicSerializer;
import de.thedead2.progression_reloaded.data.quest.PreQuestManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestManager;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.RewardStrategy;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.*;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class ProgressionLevel implements ModRegistriesDynamicSerializer {
    public static final ProgressionLevel CREATIVE = new ProgressionLevel(-1,
            "creative-level",
            new ResourceLocation(ModHelper.MOD_ID, "creative_level"),
            RewardStrategy.ALL,
            new PreQuestManager(Collections.emptySet(), new ResourceLocation(ModHelper.MOD_ID, "creative_level")), Collections.emptySet(), null, null);
    public static final ProgressionLevel TEST = new ProgressionLevel(0, "test", ResourceLocation.tryBuild(MOD_ID, "test-level"), RewardStrategy.ALL, new PreQuestManager(Set.of(ProgressionQuest.Test().getId(), ProgressionQuest.Test2().getId()), ResourceLocation.tryBuild(MOD_ID, "test-level")), Set.of(new ItemReward(Items.ITEM_FRAME.getDefaultInstance(), 5)), null, ResourceLocation.tryBuild(MOD_ID, "test-level2"));
    public static final ProgressionLevel TEST2 = new ProgressionLevel(1, "test2", ResourceLocation.tryBuild(MOD_ID, "test-level2"), RewardStrategy.ALL, new PreQuestManager(Set.of(ProgressionQuest.Test3().getId(), ProgressionQuest.Test4().getId()), ResourceLocation.tryBuild(MOD_ID, "test-level2")), Set.of(new ItemReward(Items.HORSE_SPAWN_EGG.getDefaultInstance(), 1)), ResourceLocation.tryBuild(MOD_ID, "test-level"), null);
    private final int index;
    private final String name;
    private final ResourceLocation id;
    private final Set<IReward> rewards;
    private final RewardStrategy rewardStrategy;
    @Nullable private final ResourceLocation previousLevel;
    @Nullable private final ResourceLocation nextLevel;
    private QuestManager quests;

    public ProgressionLevel(int index, String name, ResourceLocation id, RewardStrategy rewardStrategy, PreQuestManager quests, Set<IReward> rewards, @Nullable ResourceLocation previousLevel, @Nullable ResourceLocation nextLevel) {
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
        PreQuestManager questManager = PreQuestManager.fromJson(jsonObject.get("quests"));
        JsonArray rewards = jsonObject.get("rewards").getAsJsonArray();
        Set<IReward> levelRewards = new HashSet<>();
        rewards.forEach(jsonElement1 -> levelRewards.add(IReward.createFromJson(jsonElement1.getAsJsonObject())));

        return new ProgressionLevel(index, name, id, strategy, questManager, levelRewards, previous, next);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("index", this.index);
        jsonObject.addProperty("name", this.name);
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.addProperty("rewards_strategy", this.rewardStrategy.toString());
        if(this.previousLevel != null) jsonObject.addProperty("previous", this.previousLevel.toString());
        if(this.nextLevel != null) jsonObject.addProperty("next", this.nextLevel.toString());
        jsonObject.add("quests", this.quests.toJson());
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

    public QuestManager getQuestManager() {
        return this.quests;
    }

    public @Nullable ResourceLocation getNextLevel(){
        return this.nextLevel;
    }

    public void updateQuestManager() {
        this.quests = this.quests.convert();
    }
}
