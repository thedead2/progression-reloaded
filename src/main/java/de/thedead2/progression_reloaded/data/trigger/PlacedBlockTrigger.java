package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.BlockPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class PlacedBlockTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("placed_block");
    private final BlockPredicate block;
    public PlacedBlockTrigger(PlayerPredicate player, BlockPredicate block) {
        super(ID, player);
        this.block = block;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, trigger -> this.block.matches((BlockState) data[0], data[1], data[2]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("block", this.block.toJson());
    }

    public static PlacedBlockTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        BlockPredicate block = BlockPredicate.fromJson(jsonObject.get("block"));
        return new PlacedBlockTrigger(player, block);
    }
    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event){
        fireTrigger(PlacedBlockTrigger.class, event.getEntity(), event.getPlacedBlock(), event.getLevel(), event.getPlacedAgainst());
    }
}
