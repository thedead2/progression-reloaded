package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;


public class NbtPredicate implements ITriggerPredicate<Tag> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("nbt");

    public static final NbtPredicate ANY = new NbtPredicate(null);

    @Nullable
    private final CompoundTag tag;


    public NbtPredicate(@Nullable CompoundTag pTag) {
        this.tag = pTag;
    }


    public static NbtPredicate fromJson(@Nullable JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            CompoundTag compoundtag;
            try {
                compoundtag = TagParser.parseTag(GsonHelper.convertToString(pJson, "nbt"));
            }
            catch(CommandSyntaxException commandsyntaxexception) {
                throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
            }

            return new NbtPredicate(compoundtag);
        }
        else {
            return ANY;
        }
    }


    public static CompoundTag getEntityTagToCompare(Entity pEntity) {
        CompoundTag compoundtag = pEntity.saveWithoutId(new CompoundTag());
        if(pEntity instanceof Player player) {
            ItemStack itemstack = player.getInventory().getSelected();
            if(!itemstack.isEmpty()) {
                compoundtag.put("SelectedItem", itemstack.save(new CompoundTag()));
            }
        }

        return compoundtag;
    }


    public static NbtPredicate from(Tag tag) {
        try {
            return tag != null ? new NbtPredicate(tag instanceof CompoundTag compoundTag ? compoundTag : TagParser.parseTag(tag.getAsString())) : ANY;
        }
        catch(CommandSyntaxException e) {
            CrashHandler.getInstance().handleException("Failed to parse tag: " + tag, e, Level.ERROR);
            return ANY;
        }
    }


    @Override
    public boolean matches(Tag tag, Object... addArgs) {
        if(tag == null) {
            return this == ANY;
        }
        else {
            return this.tag == null || NbtUtils.compareNbt(this.tag, tag, true);
        }
    }


    @Override
    public JsonElement toJson() {
        return this != ANY && this.tag != null ? new JsonPrimitive(this.tag.toString()) : JsonNull.INSTANCE;
    }
}
