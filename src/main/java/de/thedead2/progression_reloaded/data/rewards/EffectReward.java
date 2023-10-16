package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;


public class EffectReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("effect");

    private final MobEffectInstance effect;

    //TODO: Check effect to be longer that 2 seconds otherwise it is endless --> minecraft bug?!
    public EffectReward(MobEffectInstance effect) {
        this.effect = effect;
    }


    public static EffectReward fromJson(JsonElement jsonElement) {
        return new EffectReward(JsonHelper.effectInstanceFromJson(jsonElement.getAsJsonObject()));
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        player.addEffect(effect);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        return JsonHelper.effectInstanceToJson(this.effect);
    }

}
