package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.BlockPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class PlacedBlockCriterionTrigger extends SimpleCriterionTrigger<BlockState> {

    public static final ResourceLocation ID = createId("placed_block");


    public PlacedBlockCriterionTrigger(BlockPredicate block) {
        this(block, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public PlacedBlockCriterionTrigger(BlockPredicate block, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, block, amount, duration, "block");
    }


    protected static PlacedBlockCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        BlockPredicate block = BlockPredicate.fromJson(jsonObject.get("block"));
        return new PlacedBlockCriterionTrigger(block, amount, duration);
    }


    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        fireTrigger(PlacedBlockCriterionTrigger.class, event.getEntity(), event.getPlacedBlock(), event.getEntity().getLevel().getBlockEntity(event.getPos()));
    }


    @Override
    public boolean trigger(PlayerData player, BlockState block, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(block, data[0]));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Place ").append(this.predicate.getDefaultDescription());
    }
}
