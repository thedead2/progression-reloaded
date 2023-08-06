package de.thedead2.progression_reloaded.data.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.AbilityManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BlockAbility extends ListAbility<BlockState> {
    public static final ResourceLocation ID = IAbility.createId("block");
    protected BlockAbility(boolean blacklist, Collection<BlockState> usable) {
        super(blacklist, usable);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("blacklist", this.blacklist);
        JsonArray jsonArray = new JsonArray();
        this.usable.forEach(blockState -> jsonArray.add(Block.getId(blockState)));
        jsonObject.add("usable", jsonArray);
        return jsonObject;
    }

    public static BlockAbility fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boolean blacklist = jsonObject.get("blacklist").getAsBoolean();
        Set<BlockState> blockStates = new HashSet<>();
        JsonArray jsonArray = jsonObject.get("usable").getAsJsonArray();
        jsonArray.forEach(jsonElement1 -> blockStates.add(Block.stateById(jsonElement1.getAsInt())));
        return new BlockAbility(blacklist, blockStates);
    }

    @SubscribeEvent
    public static void onBlockRightClick(final PlayerInteractEvent.RightClickBlock event){
        AbilityManager.handleAbilityRequest(BlockAbility.class, event.getEntity(), event.getEntity().getBlockStateOn(), blockState -> event.setUseBlock(Event.Result.DENY));
    }

    @SubscribeEvent
    public static void onBlockLeftClick(final PlayerInteractEvent.LeftClickBlock event){
        AbilityManager.handleAbilityRequest(BlockAbility.class, event.getEntity(), event.getEntity().getBlockStateOn(), blockState -> event.setUseBlock(Event.Result.DENY));
    }
}
