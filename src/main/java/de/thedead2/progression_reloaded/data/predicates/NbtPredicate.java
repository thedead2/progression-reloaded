package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


public class NbtPredicate implements ITriggerPredicate<Tag> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("nbt");

    public static final NbtPredicate ANY = new NbtPredicate(null);

    @Nullable
    private final CompoundTag tag;


    public NbtPredicate(@Nullable CompoundTag pTag) {
        this.tag = pTag;
    }


    public static NbtPredicate fromJson(@Nullable JsonElement jsonElement) {
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            CompoundTag compoundtag;
            try {
                compoundtag = TagParser.parseTag(jsonElement.getAsString());
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


    @Override
    public Component getDefaultDescription() {
        return Component.empty();
    }
}
