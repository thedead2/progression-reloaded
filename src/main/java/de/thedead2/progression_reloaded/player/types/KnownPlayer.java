package de.thedead2.progression_reloaded.player.types;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static de.thedead2.progression_reloaded.util.ModHelper.DATE_TIME_FORMATTER;


public record KnownPlayer(ResourceLocation id, String name, LocalDateTime lastOnline) {

    public static KnownPlayer fromPlayer(Player player) {
        return new KnownPlayer(PlayerData.createId(player.getStringUUID()), player.getScoreboardName(), LocalDateTime.now());
    }


    public static KnownPlayer fromSinglePlayer(PlayerData playerData) {
        return new KnownPlayer(playerData.getId(), playerData.getPlayerName(), LocalDateTime.now());
    }


    public static KnownPlayer fromCompoundTag(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("id"));
        String playerName = tag.getString("name");
        LocalDateTime lastLogin;
        try {
            lastLogin = DATE_TIME_FORMATTER.parse(tag.getString("lastOnline"), LocalDateTime::from);
        }
        catch(DateTimeParseException e) {
            CrashHandler.getInstance().handleException("Failed to read date from known player tag!", e, Level.ERROR);
            lastLogin = LocalDateTime.now();
        }
        return new KnownPlayer(id, playerName, lastLogin);
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        KnownPlayer that = (KnownPlayer) o;
        return Objects.equal(id, that.id) && Objects.equal(name, that.name);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }


    public static KnownPlayer fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        String name = buf.readUtf();
        LocalDateTime lastLogin = DATE_TIME_FORMATTER.parse(buf.readUtf(), LocalDateTime::from);

        return new KnownPlayer(id, name, lastLogin);
    }


    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("name", name);
        tag.putString("lastOnline", DATE_TIME_FORMATTER.format(this.lastOnline));
        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeUtf(this.name);
        buf.writeUtf(DATE_TIME_FORMATTER.format(this.lastOnline));
    }
}
