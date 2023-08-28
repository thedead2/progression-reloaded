package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public class ItemReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("item");

    private final ItemStack item;

    private final int amount;


    public ItemReward(ItemStack item, int amount) {
        this.item = item;
        this.amount = amount;
    }


    public static ItemReward fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ItemStack item = JsonHelper.itemFromJson(jsonObject.get("item").getAsJsonObject());
        int amount = jsonObject.get("amount").getAsInt();
        return new ItemReward(item, amount);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        this.item.setCount(this.amount);
        player.getInventory().add(this.item);
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("item", JsonHelper.itemToJson(this.item));
        jsonObject.addProperty("amount", this.amount);
        return jsonObject;
    }
}
