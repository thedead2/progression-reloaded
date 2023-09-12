package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.display.RewardsDisplayInfo;
import de.thedead2.progression_reloaded.player.types.PlayerData;

import java.util.HashSet;
import java.util.Set;


public class Rewards {

    private final Set<IReward> rewards;

    private final RewardStrategy rewardStrategy;


    public Rewards(Set<IReward> rewards, RewardStrategy rewardStrategy) {
        this.rewards = rewards;
        this.rewardStrategy = rewardStrategy;
    }


    public static Rewards fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        RewardStrategy strategy = RewardStrategy.valueOf(jsonObject.get("strategy").getAsString());
        JsonArray array = jsonObject.get("rewards").getAsJsonArray();
        Set<IReward> rewards = new HashSet<>();
        array.forEach(jsonElement1 -> rewards.add(IReward.createFromJson(jsonElement1.getAsJsonObject())));

        return new Rewards(rewards, strategy);
    }


    public void reward(PlayerData player) {
        this.rewardStrategy.reward(this.rewards, player);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("strategy", this.rewardStrategy.toString());
        JsonArray jsonArray = new JsonArray();
        this.rewards.forEach(reward -> jsonArray.add(reward.saveToJson()));
        jsonObject.add("rewards", jsonArray);

        return jsonObject;
    }


    public RewardsDisplayInfo getDisplay() {
        return null;
    }


    public static class Builder {

        private final Set<IReward> rewards = new HashSet<>();

        private RewardStrategy strategy = RewardStrategy.ALL;


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withReward(IReward reward) {
            this.rewards.add(reward);

            return this;
        }


        public Builder withStrategy(RewardStrategy strategy) {
            this.strategy = strategy;

            return this;
        }


        public Rewards build() {
            return new Rewards(this.rewards, this.strategy);
        }
    }
}
