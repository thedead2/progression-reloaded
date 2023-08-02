package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class EntityPredicate implements ITriggerPredicate<EntityType<?>> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("entity");
    private final EntityType<?> type;

    public EntityPredicate(EntityType<?> type) {
        this.type = type;
    }

    public static EntityPredicate fromJson(JsonElement killedEntity) {
        return null;
    }

    @Override
    public boolean matches(EntityType<?> entityType, Object... addArgs) {
        boolean flag = false;
        flag = entityType.equals(this.type);

        return flag;
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        JsonPrimitive type = new JsonPrimitive(ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
        return null;
    }

    @Override
    public Builder<EntityPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<EntityType<?>> copy() {
        return null;
    }

    public static EntityPredicate from(EntityType<?> entityType) {
        return new EntityPredicate(entityType);
    }
}
