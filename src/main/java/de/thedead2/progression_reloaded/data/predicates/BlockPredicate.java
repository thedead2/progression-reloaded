package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class BlockPredicate implements ITriggerPredicate<BlockState> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("block");
    @SuppressWarnings("unchecked")
    public static final BlockPredicate ANY = new BlockPredicate(null, null, (StatePropertiesPredicate<BlockState>) StatePropertiesPredicate.ANY, NbtPredicate.ANY);
    @Nullable
    private final TagKey<Block> tag;
    @Nullable
    private final Block block;
    private final StatePropertiesPredicate<BlockState> properties;
    private final NbtPredicate nbt;

    public BlockPredicate(@Nullable TagKey<Block> tag, @Nullable Block block, StatePropertiesPredicate<BlockState> properties, NbtPredicate nbt) {
        this.tag = tag;
        this.block = block;
        this.properties = properties;
        this.nbt = nbt;
    }

    public static BlockPredicate fromJson(JsonElement jsonElement) {
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonElement, "block");
            NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
            Block block1 = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(GsonHelper.getAsString(jsonobject, "block", null)));

            TagKey<Block> tagkey = null;
            if (jsonobject.has("tag")) {
                ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag"));
                tagkey = TagKey.create(Registries.BLOCK, resourcelocation1);
            }

            StatePropertiesPredicate<BlockState> statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("state"));
            return new BlockPredicate(tagkey, block1, statepropertiespredicate, nbtpredicate);
        } else {
            return ANY;
        }
    }

    @Override
    public boolean matches(BlockState blockState, Object... addArgs) {
        if (this == ANY) {
            return true;
        } else {
            if (this.tag != null && !blockState.is(this.tag)) {
                return false;
            } else if (this.block != null && !this.block.equals(blockState.getBlock())) {
                return false;
            } else if (!this.properties.matches(blockState)) {
                return false;
            } else {
                if (this.nbt != NbtPredicate.ANY) {
                    BlockEntity blockentity = addArgs[0] != null ? (BlockEntity) addArgs[0] : null;
                    return blockentity != null && this.nbt.matches(blockentity.saveWithFullMetadata());
                }

                return true;
            }
        }
    }

    @Override
    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject jsonobject = new JsonObject();
            if (this.block != null) {

                jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(block).toString());
            }

            if (this.tag != null) {
                jsonobject.addProperty("tag", this.tag.location().toString());
            }

            jsonobject.add("nbt", this.nbt.toJson());
            jsonobject.add("state", this.properties.toJson());
            return jsonobject;
        }
    }

    public static BlockPredicate from(BlockState blockState, Object... addArgs) {
        if(blockState == null) return ANY;
        TagKey<Block> blockTagKey = blockState.getTags().findFirst().orElse(null);
        Block block1 = blockState.getBlock();
        StatePropertiesPredicate<BlockState> properties = StatePropertiesPredicate.from(blockState);
        NbtPredicate nbt = addArgs[0] != null ? NbtPredicate.from(((BlockEntity) addArgs[0]).saveWithFullMetadata()) : NbtPredicate.ANY;
        return new BlockPredicate(blockTagKey, block1, properties, nbt);
    }

}
