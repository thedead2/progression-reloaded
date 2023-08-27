package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class EntityTypePredicate implements ITriggerPredicate<EntityType<?>> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("entity_type");
    public static final EntityTypePredicate ANY = new EntityTypePredicate(null, null);
    @Nullable
    private final EntityType<?> type;
    @Nullable
    private final TagKey<EntityType<?>> tag;

    public EntityTypePredicate(@Nullable EntityType<?> type, @Nullable TagKey<EntityType<?>> tag) {
        this.type = type;
        this.tag = tag;
    }

    public static EntityTypePredicate fromJson(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            String s = GsonHelper.convertToString(jsonElement, "type");
            if (s.startsWith("#")) {
                return new EntityTypePredicate(null, TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(s.substring(1))));
            } else {
                ResourceLocation resourcelocation = new ResourceLocation(s);
                EntityType<?> entitytype = ForgeRegistries.ENTITY_TYPES.getValue(resourcelocation);
                return new EntityTypePredicate(entitytype, null);
            }
        } else {
            return ANY;
        }
    }

    public static EntityTypePredicate from(EntityType<?> type) {
        if(type == null) return ANY;
        return new EntityTypePredicate(type, null);
    }

    @Override
    public boolean matches(EntityType<?> entityType, Object... addArgs) {
        if(this == ANY) return true;
        else {
            if(this.type != null) return this.type == entityType;
            else if(this.tag != null) return entityType.is(this.tag);
        }
        return true;
    }

    @Override
    public JsonElement toJson() {
        if(this == ANY) return JsonNull.INSTANCE;
        else {
            if(this.type != null) return new JsonPrimitive(ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
            else if(this.tag != null) return new JsonPrimitive("#" + this.tag.location());
        }
        return JsonNull.INSTANCE;
    }
}
