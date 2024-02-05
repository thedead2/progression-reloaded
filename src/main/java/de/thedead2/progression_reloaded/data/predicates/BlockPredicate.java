package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;


public class BlockPredicate implements ITriggerPredicate<BlockState> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("block");

    public static final BlockPredicate ANY = new BlockPredicate(null, null, NbtPredicate.ANY);

    @Nullable
    private final TagKey<Block> tag;

    @Nullable
    private final Block block;

    private final NbtPredicate nbt;


    public BlockPredicate(@Nullable TagKey<Block> tag, @Nullable Block block, NbtPredicate nbt) {
        this.tag = tag;
        this.block = block;
        this.nbt = nbt;
    }


    public static BlockPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            JsonObject jsonobject = jsonElement.getAsJsonObject();
            Block block = SerializationHelper.getNullable(jsonobject, "block", jsonElement1 -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(jsonElement1.getAsString())));
            TagKey<Block> tag = SerializationHelper.getNullable(jsonobject, "tag", jsonElement1 -> {
                ResourceLocation resourcelocation1 = new ResourceLocation(jsonElement1.getAsString());
                return TagKey.create(Registries.BLOCK, resourcelocation1);
            });
            NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));

            return new BlockPredicate(tag, block, nbtPredicate);
        }
        else {
            return ANY;
        }
    }


    public static BlockPredicate from(Block block) {
        return new BlockPredicate(null, block, NbtPredicate.ANY);
    }

    @Override
    public boolean matches(BlockState blockState, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        else {
            if(this.tag != null && !blockState.is(this.tag)) {
                return false;
            }
            else if(this.block != null && !this.block.equals(blockState.getBlock())) {
                return false;
            }
            else {
                if(this.nbt != NbtPredicate.ANY) {
                    BlockEntity blockentity = addArgs[0] != null ? (BlockEntity) addArgs[0] : null;
                    return blockentity != null && this.nbt.matches(blockentity.saveWithFullMetadata());
                }

                return true;
            }
        }
    }


    @Override
    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();

            SerializationHelper.addNullable(this.block, jsonobject, "block", block1 -> new JsonPrimitive(ForgeRegistries.BLOCKS.getKey(block1).toString()));
            SerializationHelper.addNullable(this.tag, jsonobject, "tag", tag -> new JsonPrimitive(tag.location().toString()));

            jsonobject.add("nbt", this.nbt.toJson());
            return jsonobject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        if(this == ANY) {
            return Component.literal(" blocks");
        }
        else {
            if(this.tag != null) {
                return Component.literal(" " + this.tag.location().getPath());
            }
            else {
                return Component.literal(" ").append(this.block.getName());
            }
        }
    }

}
