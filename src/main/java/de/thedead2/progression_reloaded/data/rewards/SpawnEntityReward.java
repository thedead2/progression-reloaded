package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;


public class SpawnEntityReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("spawn_entity");

    private final EntityType<?> entityType;

    private final int amount;


    public SpawnEntityReward(EntityType<?> entityType) {
        this(entityType, 1);
    }


    public SpawnEntityReward(EntityType<?> entityType, int amount) {
        this.entityType = entityType;
        this.amount = amount;
    }


    public static SpawnEntityReward fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        EntityType<?> entityType1 = EntityType.byString(jsonObject.get("entity_type").getAsString()).orElseThrow(() -> new RuntimeException("Unknown Entity with id: " + jsonObject.get("entity_type").getAsString()));
        int amount = jsonObject.get("amount").getAsInt();

        return new SpawnEntityReward(entityType1, amount);
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        for(int i = 0; i < amount; i++) {
            entityType.spawn(player.getLevel(), null, player, player.blockPosition(), MobSpawnType.COMMAND, true, false);
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("entity_type", EntityType.getKey(this.entityType).toString());
        jsonObject.addProperty("amount", this.amount);
        return jsonObject;
    }
}
