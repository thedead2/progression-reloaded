package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.JsonHelper;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectReward implements IReward{
    public static final ResourceLocation ID = IReward.createId("effect");
    private final MobEffectInstance effect;

    public EffectReward(MobEffectInstance effect) {
        this.effect = effect;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        player.addEffect(effect);
    }


    public static EffectReward fromJson(JsonElement jsonElement){
        return new EffectReward(JsonHelper.effectInstanceFromJson(jsonElement.getAsJsonObject()));
    }
    @Override
    public JsonElement toJson() {
        return JsonHelper.effectInstanceToJson(this.effect);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

}
