package de.thedead2.progression_reloaded.data.restrictions;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.network.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
        TagKey<R> tag = buf.readNullable(NetworkHelper::readTagKey);

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
        buf.writeNullable(this.tag, NetworkHelper::writeTagKey);
    }
}
