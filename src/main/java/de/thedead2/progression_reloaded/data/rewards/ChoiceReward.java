package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class ChoiceReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("choice");

    private final List<IReward> possibleRewards;

    private final int amountSelectable;


    public ChoiceReward(List<IReward> possibleRewards, int amountSelectable) {
        this.possibleRewards = possibleRewards;
        this.amountSelectable = amountSelectable;
    }


    public static ChoiceReward fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int amountSelectable = jsonObject.get("amount_selectable").getAsInt();
        List<IReward> possibleRewards = new ArrayList<>();
        JsonArray jsonArray = jsonObject.get("possible_rewards").getAsJsonArray();
        jsonArray.forEach(jsonElement1 -> possibleRewards.add(IReward.createFromJson(jsonElement)));

        return new ChoiceReward(possibleRewards, amountSelectable);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        for(IReward reward : getChosen()) {
            reward.rewardPlayer(player);
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount_selectable", this.amountSelectable);

        JsonArray jsonArray = new JsonArray();
        this.possibleRewards.forEach(iReward -> jsonArray.add(iReward.saveToJson()));
        jsonObject.add("possible_rewards", jsonArray);

        return jsonObject;
    }


    private Collection<IReward> getChosen() {
        return Collections.emptyList();
    }
}
