package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.BlockPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BreakBlockTrigger extends SimpleTrigger{
    public static final ResourceLocation ID = createId("break_block");
    private final BlockPredicate block;
    protected BreakBlockTrigger(PlayerPredicate player, BlockPredicate block) {
        super(ID, player);
        this.block = block;
    }

    @Override
    public boolean trigger(SinglePlayer player, Object... data) {
        return this.trigger(player, listener -> this.block.matches((BlockState) data[0], data[1]));
    }

    @Override
    public void toJson(JsonObject data) {
        data.add("block", this.block.toJson());
    }

    public static BreakBlockTrigger fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new BreakBlockTrigger(PlayerPredicate.fromJson(jsonObject.get("player")), BlockPredicate.fromJson(jsonObject.get("block")));
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event){
        fireTrigger(BreakBlockTrigger.class, event.getPlayer(), event.getLevel().getBlockState(event.getPos()), event.getLevel().getBlockEntity(event.getPos()));
    }
}
