package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class AbilityReward implements IReward{
    public static final ResourceLocation ID = IReward.createId("ability");
    private final Set<IAbility<?>> abilities;

    public AbilityReward(Set<IAbility<?>> abilities) {
        this.abilities = abilities;
    }

    public AbilityReward(IAbility<?>... abilities) {
        this.abilities = Set.of(abilities);
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        PlayerDataHandler.getPlayerData().orElseThrow().getActivePlayer(player).addAbilities(this.abilities);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        this.abilities.forEach(iAbility -> array.add(iAbility.toJson()));
        return array;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static AbilityReward fromJson(JsonElement jsonArray){
        JsonArray array = jsonArray.getAsJsonArray();
        Set<IAbility<?>> abilities1 = new HashSet<>();
        array.forEach(jsonElement -> abilities1.add(IAbility.createFromJson(jsonElement)));
        return new AbilityReward(abilities1);
    }
}
