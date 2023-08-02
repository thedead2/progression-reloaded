package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        return new EffectReward(createEffectInstanceFromJson(jsonElement.getAsJsonObject()));
    }
    @Override
    public JsonElement toJson() {
        return effectInstanceToJson(this.effect);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public JsonObject effectInstanceToJson(MobEffectInstance effect){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("effect", MobEffect.getId(effect.getEffect()));
        jsonObject.addProperty("duration", effect.getDuration());
        jsonObject.addProperty("amplifier", effect.getAmplifier());
        jsonObject.addProperty("ambient", effect.isAmbient());
        jsonObject.addProperty("visible", effect.isVisible());
        jsonObject.addProperty("showIcon", effect.showIcon());

        return jsonObject;
    }

    private static MobEffectInstance createEffectInstanceFromJson(JsonObject jsonObject){
        MobEffect effect1;
        int duration, amplifier;
        boolean ambient, visible, showIcon;

        effect1 = MobEffect.byId(jsonObject.get("effect").getAsInt());
        duration = jsonObject.get("duration").getAsInt();
        amplifier = jsonObject.get("amplifier").getAsInt();
        ambient = jsonObject.get("ambient").getAsBoolean();
        visible = jsonObject.get("visible").getAsBoolean();
        showIcon = jsonObject.get("showIcon").getAsBoolean();


        return new MobEffectInstance(effect1, duration, amplifier, ambient, visible, showIcon);
    }
}
