package de.thedead2.progression_reloaded.data.abilities;

import com.google.common.base.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stdlib.Strings;


public class RestrictionKey<T> {

    @Nullable
    private final ResourceLocation id;

    @Nullable
    private final String modId;

    @Nullable
    private final TagKey<T> tag;


    private RestrictionKey(@NotNull ResourceLocation id) {
        this(id, null, null);
    }


    private RestrictionKey(@Nullable ResourceLocation id, @Nullable String modId, @Nullable TagKey<T> tag) {
        this.id = id;
        this.modId = modId;
        this.tag = tag;
    }


    private RestrictionKey(@NotNull String modId) {
        this(null, modId, null);
    }


    private RestrictionKey(@NotNull TagKey<T> tag) {
        this(null, null, tag);
    }


    @SuppressWarnings("unchecked")
    public static <T> RestrictionKey<T> wrap(@NotNull Object o) {
        if(isValidKey(o)) {
            if(o instanceof ResourceLocation id) {
                return wrap(id);
            }
            else if(o instanceof String modId) {
                return wrap(modId);
            }
            else {
                return wrap((TagKey<T>) o);
            }
        }
        throw new IllegalArgumentException("Can't create RestrictionKey from object: " + o.getClass().getName());
    }


    public static boolean isValidKey(Object o) {
        return o instanceof ResourceLocation || o instanceof String || o instanceof TagKey<?>;
    }


    public static <T> RestrictionKey<T> wrap(@NotNull ResourceLocation id) {
        return new RestrictionKey<>(id);
    }


    public static <T> RestrictionKey<T> wrap(@NotNull String modId) {
        return new RestrictionKey<>(modId);
    }


    public static <T> RestrictionKey<T> wrap(@NotNull TagKey<T> tag) {
        return new RestrictionKey<>(tag);
    }


    public static <R> RestrictionKey<R> fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        String modId = buf.readNullable(FriendlyByteBuf::readUtf);
        TagKey<R> tag = buf.readNullable(buf1 -> {
            String s = buf1.readUtf();
            String[] keys = Strings.split(s, '|');
            ResourceLocation registryLocation = new ResourceLocation(keys[0]);
            ResourceLocation tagLocation = new ResourceLocation(keys[1]);
            ResourceKey<Registry<Object>> registryId = ResourceKey.createRegistryKey(registryLocation);

            return (TagKey<R>) TagKey.create(registryId, tagLocation);
        });

        return new RestrictionKey<>(id, modId, tag);
    }


    public boolean is(Object o) {
        if(o instanceof ResourceLocation oId) {
            return this.id != null && this.id.equals(oId);
        }
        else if(o instanceof String oModId) {
            return this.modId != null && this.modId.equals(oModId);
        }
        else if(o instanceof TagKey<?> oTag) {
            return this.tag != null && this.tag.equals(oTag);
        }
        else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(id, modId, tag);
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        RestrictionKey<?> that = (RestrictionKey<?>) o;
        return Objects.equal(id, that.id) && Objects.equal(modId, that.modId) && Objects.equal(tag, that.tag);
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNullable(this.id, FriendlyByteBuf::writeResourceLocation);
        buf.writeNullable(this.modId, FriendlyByteBuf::writeUtf);
        buf.writeNullable(this.tag, (buf1, tTagKey) -> buf1.writeUtf(tTagKey.registry().location() + "|" + tTagKey.location()));
    }
}
