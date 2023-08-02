package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class PlayerPredicate implements ITriggerPredicate<SinglePlayer> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("player");

    public static final PlayerPredicate ANY = new PlayerPredicate(EnchantmentPredicate.ANY);
    private final EnchantmentPredicate enchantments;

    public PlayerPredicate(EnchantmentPredicate enchantments) {
        this.enchantments = enchantments;
    }

    public static PlayerPredicate fromJson(JsonElement player) {
        return null;
    }

    @Override
    public boolean matches(SinglePlayer player, Object... addArgs) {
        if(this == ANY) return true;
        return false;
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public Builder<PlayerPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<SinglePlayer> copy() {
        return null;
    }

    public static PlayerPredicate from(SinglePlayer player) {
        return null;
    }

}
