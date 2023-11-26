package de.thedead2.progression_reloaded.player.types;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static de.thedead2.progression_reloaded.util.ModHelper.DATE_TIME_FORMATTER;


public record KnownPlayer(UUID uuid, String name, LocalDateTime lastOnline) {

    public static KnownPlayer fromPlayer(Player player) {
        return new KnownPlayer(player.getUUID(), player.getScoreboardName(), LocalDateTime.now());
    }


    public static KnownPlayer fromSinglePlayer(PlayerData playerData) {
        return new KnownPlayer(playerData.getUUID(), playerData.getName(), LocalDateTime.now());
    }


    public static KnownPlayer fromCompoundTag(CompoundTag tag) {
        UUID uuid = tag.getUUID("uuid");
        String playerName = tag.getString("name");
        LocalDateTime lastLogin;
        try {
            lastLogin = DATE_TIME_FORMATTER.parse(tag.getString("lastOnline"), LocalDateTime::from);
        }
        catch(DateTimeParseException e) {
            CrashHandler.getInstance().handleException("Failed to read date from known player tag!", e, Level.ERROR);
            lastLogin = LocalDateTime.now();
        }
        return new KnownPlayer(uuid, playerName, lastLogin);
    }


    public static KnownPlayer fromString(String s) {
        if(!s.contains(":") || !s.contains("/")) {
            throw new IllegalArgumentException();
        }
        String stringUUID = s.substring(0, s.indexOf(':'));
        String name = s.substring(s.indexOf(':') + 1, s.indexOf('/'));
        String formattedTime = s.substring(s.indexOf('/') + 1);

        UUID uuid = UUID.fromString(stringUUID);
        LocalDateTime lastOnline = DATE_TIME_FORMATTER.parse(formattedTime, LocalDateTime::from);

        return new KnownPlayer(uuid, name, lastOnline);
    }


    public static KnownPlayer fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        String name = buf.readUtf();
        LocalDateTime lastLogin = DATE_TIME_FORMATTER.parse(buf.readUtf(), LocalDateTime::from);

        return new KnownPlayer(uuid, name, lastLogin);
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
        return Objects.equal(uuid, that.uuid) && Objects.equal(name, that.name);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, name);
    }


    @Override
    public String toString() {
        return this.uuid.toString() + ":" + this.name + "/" + DATE_TIME_FORMATTER.format(this.lastOnline);
    }


    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", name);
        tag.putString("lastOnline", DATE_TIME_FORMATTER.format(this.lastOnline));
        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeUtf(this.name);
        buf.writeUtf(DATE_TIME_FORMATTER.format(this.lastOnline));
    }
}
