package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;


public class PlayerChangedDimensionCriterionTrigger extends SimpleCriterionTrigger<ResourceKey<Level>> {

    public static final ResourceLocation ID = createId("changed_dimension");

    @Nullable
    private final ResourceKey<Level> from;

    @Nullable
    private final ResourceKey<Level> to;


    public PlayerChangedDimensionCriterionTrigger(@Nullable ResourceKey<Level> from, @Nullable ResourceKey<Level> to) {
        this(from, to, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlayerChangedDimensionCriterionTrigger(@Nullable ResourceKey<Level> from, @Nullable ResourceKey<Level> to, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, null, amount, duration, "");
        this.from = from;
        this.to = to;
    }


    protected static PlayerChangedDimensionCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceKey<Level> from = SerializationHelper.getNullable(jsonObject, "from", jsonElement1 -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation(jsonElement1.getAsString())));
        ResourceKey<Level> to = SerializationHelper.getNullable(jsonObject, "to", jsonElement1 -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation(jsonElement1.getAsString())));

        return new PlayerChangedDimensionCriterionTrigger(from, to, amount, duration);
    }


    @SubscribeEvent
    public static void onDimensionChanged(final PlayerEvent.PlayerChangedDimensionEvent event) {
        fireTrigger(PlayerChangedDimensionCriterionTrigger.class, event.getEntity(), event.getFrom(), event.getTo());
    }


    @Override
    public boolean trigger(PlayerData player, ResourceKey<Level> from, Object... data) {
        return this.trigger(player, listener -> {
            if(this.from != null && this.from != from) {
                return false;
            }
            else {
                return this.to == null || this.to == data[0];
            }
        });
    }


    @Override
    protected void toJson(JsonObject jsonObject) {
        SerializationHelper.addNullable(this.from, jsonObject, "from", from -> new JsonPrimitive(from.location().toString()));
        SerializationHelper.addNullable(this.to, jsonObject, "to", to -> new JsonPrimitive(to.location().toString()));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Travel from the ").append(this.from.location().getPath()).append(" to the ").append(this.to.location().getPath());
    }
}
