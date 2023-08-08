package de.thedead2.progression_reloaded.data.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.AbilityManager;
import de.thedead2.progression_reloaded.util.JsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UseItemAbility extends ListAbility<ItemStack> {
    public static final ResourceLocation ID = IAbility.createId("used_item");
    protected UseItemAbility(boolean blacklist, Collection<ItemStack> usable) {
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
        this.usable.forEach(itemStack -> jsonArray.add(JsonHelper.itemToJson(itemStack)));
        jsonObject.add("usable", jsonArray);
        return jsonObject;
    }

    public static UseItemAbility fromJson(JsonElement jsonElement){
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boolean blacklist = jsonObject.get("blacklist").getAsBoolean();
        Set<ItemStack> itemStacks = new HashSet<>();
        JsonArray jsonArray = jsonObject.get("usable").getAsJsonArray();
        jsonArray.forEach(jsonElement1 -> itemStacks.add(JsonHelper.itemFromJson(jsonElement1.getAsJsonObject())));
        return new UseItemAbility(blacklist, itemStacks);
    }

    @SubscribeEvent
    public static void onItemUse(final LivingEntityUseItemEvent.Start event){
        AbilityManager.handleAbilityRequest(UseItemAbility.class, event.getEntity(), event.getItem(), item -> event.setCanceled(true));
    }
}
