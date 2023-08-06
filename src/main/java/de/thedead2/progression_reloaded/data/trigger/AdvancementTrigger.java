package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.AdvancementPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class AdvancementTrigger extends SimpleTrigger {
    public static final ResourceLocation ID = createId("advancement");
    private final AdvancementPredicate advancement;
    protected AdvancementTrigger(PlayerPredicate player, AdvancementPredicate advancement) {
        super(ID, player);
        this.advancement = advancement;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, trigger -> this.advancement.matches((Advancement) data[0], data[1]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("advancement", this.advancement.toJson());
    }

    public static AdvancementTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        AdvancementPredicate advancement = AdvancementPredicate.fromJson(jsonObject.get("advancement"));
        return new AdvancementTrigger(player, advancement);
    }

    @SubscribeEvent
    public static void onAdvancementGained(@SuppressWarnings("all") final AdvancementEvent.AdvancementProgressEvent event){
        fireTrigger(AdvancementTrigger.class, event.getEntity(), event.getAdvancement(), event.getAdvancementProgress());
    }

}
