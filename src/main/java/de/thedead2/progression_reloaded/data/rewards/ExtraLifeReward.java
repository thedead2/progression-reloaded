package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;


public class ExtraLifeReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("extra_life");

    private final int amount;


    public ExtraLifeReward() {
        this(1);
    }


    public ExtraLifeReward(int amount) {this.amount = amount;}


    public static ExtraLifeReward fromJson(JsonElement jsonElement) {
        int amount = jsonElement.getAsInt();
        return new ExtraLifeReward(amount);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        for(int i = 0; i < amount; i++) {
            ExtraLifeItem.rewardExtraLife(player, true);
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(amount);
    }
}
