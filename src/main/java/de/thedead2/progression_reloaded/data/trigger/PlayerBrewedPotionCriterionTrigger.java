package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;


public class PlayerBrewedPotionCriterionTrigger extends SimpleCriterionTrigger<Potion> {

    public static final ResourceLocation ID = createId("brewed_potion");

    @Nullable
    private final Potion potion;


    public PlayerBrewedPotionCriterionTrigger(@Nullable Potion potion) {
        this(potion, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerBrewedPotionCriterionTrigger(@Nullable Potion potion, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, null, amount, duration, "");
        this.potion = potion;
    }


    protected static PlayerBrewedPotionCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Potion potion = SerializationHelper.getNullable(jsonObject, "potion", jsonElement1 -> {
            ResourceLocation resourcelocation = new ResourceLocation(jsonElement1.getAsString());
            return ForgeRegistries.POTIONS.getValue(resourcelocation);
        });

        return new PlayerBrewedPotionCriterionTrigger(potion, amount, duration);
    }


    @SubscribeEvent
    public static void onPotionBrewed(final PlayerBrewedPotionEvent event) {
        fireTrigger(PlayerBrewedPotionCriterionTrigger.class, event.getEntity(), PotionUtils.getPotion(event.getStack()));
    }


    @Override
    public boolean trigger(PlayerData player, Potion potion, Object... data) {
        return this.trigger(player, listener -> this.potion == null || this.potion == potion);
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        SerializationHelper.addNullable(this.potion, jsonObject, "potion", potion1 -> new JsonPrimitive(ForgeRegistries.POTIONS.getKey(potion1).toString()));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Brew ").append(this.amount.getDefaultDescription()).append(this.potion.getName(""));
    }
}
