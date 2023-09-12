package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.BlockPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlacedBlockTrigger extends SimpleTrigger<BlockState> {

    public static final ResourceLocation ID = createId("placed_block");


    public PlacedBlockTrigger(PlayerPredicate player, BlockPredicate block) {
        super(ID, player, block, "block");
    }


    public static PlacedBlockTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        BlockPredicate block = BlockPredicate.fromJson(jsonObject.get("block"));
        return new PlacedBlockTrigger(player, block);
    }


    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        fireTrigger(PlacedBlockTrigger.class, event.getEntity(), event.getPlacedBlock(), event.getEntity().getLevel().getBlockEntity(event.getPos()));
    }


    @Override
    public boolean trigger(PlayerData player, BlockState block, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(block, data[0]));
    }


    @Override
    public void toJson(JsonObject data) {
    }
}
