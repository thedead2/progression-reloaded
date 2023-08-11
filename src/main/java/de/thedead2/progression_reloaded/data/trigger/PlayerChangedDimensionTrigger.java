package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class PlayerChangedDimensionTrigger extends SimpleTrigger<ResourceKey<Level>>{
    public static final ResourceLocation ID = createId("changed_dimension");
    @Nullable
    private final ResourceKey<Level> from;
    @Nullable
    private final ResourceKey<Level> to;
    public PlayerChangedDimensionTrigger(PlayerPredicate player, @Nullable ResourceKey<Level> from, @Nullable ResourceKey<Level> to) {
        super(ID, player, null, "");
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean trigger(SinglePlayer player, ResourceKey<Level> from, Object... data) {
        return this.trigger(player, listener -> {
            if (this.from != null && this.from != from) {
                return false;
            }
            else {
                return this.to == null || this.to == data[0];
            }
        });
    }

    @Override
    public void toJson(JsonObject data) {
        if (this.from != null) {
            data.addProperty("from", this.from.location().toString());
        }

        if (this.to != null) {
            data.addProperty("to", this.to.location().toString());
        }
    }

    public static PlayerChangedDimensionTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceKey<Level> from = jsonObject.has("from") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
        ResourceKey<Level> to = jsonObject.has("to") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
        return new PlayerChangedDimensionTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), from, to);
    }

    @SubscribeEvent
    public static void onDimensionChanged(final PlayerEvent.PlayerChangedDimensionEvent event){
        fireTrigger(PlayerChangedDimensionTrigger.class, event.getEntity(), event.getFrom(), event.getTo());
    }
}
