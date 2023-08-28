package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;


public class XPReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("xp");

    private final int amount;

    private final boolean levels;


    public XPReward(int amount, boolean levels) {
        this.amount = amount;
        this.levels = levels;
    }


    public static XPReward fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int amount = jsonObject.get("amount").getAsInt();
        boolean levels = jsonObject.get("levels").getAsBoolean();

        return new XPReward(amount, levels);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        if(levels) {
            player.giveExperienceLevels(amount);
        }
        else {
            player.giveExperiencePoints(amount);
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", this.amount);
        jsonObject.addProperty("levels", this.levels);
        return jsonObject;
    }
}
