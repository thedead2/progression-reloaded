package de.thedead2.progression_reloaded.data.rewards;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.display.RewardsDisplayInfo;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

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


    public static Rewards empty() {
        return Builder.builder().build();
    }


    public static Rewards fromNetwork(FriendlyByteBuf buf) {
        RewardStrategy strategy = buf.readEnum(RewardStrategy.class);
        Set<IReward> rewards = buf.readCollection(Sets::newHashSetWithExpectedSize, IReward::fromNetwork);
        return new Rewards(rewards, strategy);
    }


    public static Rewards loadFromNBT(CompoundTag tag) {
        RewardStrategy strategy = RewardStrategy.valueOf(tag.getString("strategy"));
        Set<IReward> rewards = CollectionHelper.loadFromNBT(Sets::newHashSetWithExpectedSize, tag.getList("rewards", 0), tag1 -> IReward.fromNBT((CompoundTag) tag1));

        return new Rewards(rewards, strategy);
    }


    public void reward(PlayerData player) {
        this.rewardStrategy.reward(this.rewards, player);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("strategy", this.rewardStrategy.toString());
        jsonObject.add("rewards", CollectionHelper.saveToJson(this.rewards, IReward::saveToJson));

        return jsonObject;
    }


    public RewardsDisplayInfo getDisplay() {
        return null;
    }


    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("strategy", this.rewardStrategy.name());
        tag.put("rewards", CollectionHelper.saveToNBT(this.rewards, IReward::saveToNBT));

        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(this.rewardStrategy);
        buf.writeCollection(this.rewards, (buf1, reward) -> reward.toNetwork(buf1));
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


        public Builder withStrategy(@NotNull RewardStrategy strategy) {
            this.strategy = strategy;

            return this;
        }


        public Rewards build() {
            return new Rewards(this.rewards, this.strategy);
        }
    }
}
