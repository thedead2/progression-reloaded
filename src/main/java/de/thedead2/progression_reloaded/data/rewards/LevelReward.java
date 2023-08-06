package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class LevelReward implements IReward{
    public static final ResourceLocation ID = IReward.createId("level");
    private final ResourceLocation levelId;

    public LevelReward(ResourceLocation levelId) {
        this.levelId = levelId;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        LevelManager.getInstance().updateLevel(PlayerDataHandler.getActivePlayer(player), this.levelId);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.levelId.toString());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static LevelReward fromJson(JsonElement jsonElement){
        return new LevelReward(new ResourceLocation(jsonElement.getAsString()));
    }
}
