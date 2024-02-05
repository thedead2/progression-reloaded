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


public class BreakBlockCriterionTrigger extends SimpleCriterionTrigger<BlockState> {

    public static final ResourceLocation ID = createId("break_block");


    public BreakBlockCriterionTrigger(BlockPredicate block) {
        this(block, MinMax.Ints.ANY, MinMax.Doubles.ANY);
    }


    public BreakBlockCriterionTrigger(BlockPredicate block, MinMax.Ints amount, MinMax.Doubles duration) {
        super(ID, block, amount, duration, "block");
    }


    protected static BreakBlockCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new BreakBlockCriterionTrigger(BlockPredicate.fromJson(jsonObject.get("block")), amount, duration);
    }


    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        fireTrigger(BreakBlockCriterionTrigger.class, event.getPlayer(), event.getLevel().getBlockState(event.getPos()), event.getLevel().getBlockEntity(event.getPos()));
    }


    @Override
    public boolean trigger(PlayerData player, BlockState block, Object... data) {
        return this.trigger(player, listener -> this.predicate.matches(block, data[0]));
    }


    @Override
    public Component getDefaultDescription() {
        return Component.literal("Break ").append(this.amount.getDefaultDescription()).append(this.predicate.getDefaultDescription());
    }
}
