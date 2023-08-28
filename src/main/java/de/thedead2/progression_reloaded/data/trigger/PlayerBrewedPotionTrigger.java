package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;


public class PlayerBrewedPotionTrigger extends SimpleTrigger<Potion> {

    public static final ResourceLocation ID = createId("brewed_potion");

    @Nullable
    private final Potion potion;


    public PlayerBrewedPotionTrigger(PlayerPredicate player, @Nullable Potion potion) {
        super(ID, player, null, "");
        this.potion = potion;
    }


    public static PlayerBrewedPotionTrigger fromJson(JsonElement jsonElement) {
        Potion potion = null;
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if(jsonObject.has("potion")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
            potion = ForgeRegistries.POTIONS.getValue(resourcelocation);
        }
        return new PlayerBrewedPotionTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), potion);
    }


    @SubscribeEvent
    public static void onPotionBrewed(final PlayerBrewedPotionEvent event) {
        fireTrigger(PlayerBrewedPotionTrigger.class, event.getEntity(), PotionUtils.getPotion(event.getStack()));
    }


    @Override
    public boolean trigger(SinglePlayer player, Potion potion, Object... data) {
        return this.trigger(player, listener -> this.potion == null || this.potion == potion);
    }


    @Override
    public void toJson(JsonObject data) {
        if(this.potion != null) {
            data.addProperty("potion", ForgeRegistries.POTIONS.getKey(this.potion).toString());
        }
    }
}
