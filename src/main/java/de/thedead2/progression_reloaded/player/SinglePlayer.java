package de.thedead2.progression_reloaded.player;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

public class SinglePlayer {
    private final String playerName;
    private PlayerTeam team;
    private final UUID uuid;
    private final ResourceLocation id;
    private final ServerPlayer player;
    private ProgressionLevel progressionLevel;
    private boolean isOffline;

    public SinglePlayer(ServerPlayer player, ResourceLocation id) {
        this(null, player, id, new ResourceLocation(ModHelper.MOD_ID, "base_level"));
    }

    public SinglePlayer(PlayerTeam team, ServerPlayer player, ResourceLocation id, ResourceLocation progressionLevelId) {
        this.playerName = player.getScoreboardName();
        this.team = team;
        this.uuid = player.getUUID();
        this.id = id;
        this.player = player;
        this.progressionLevel = this.team != null ? this.team.getProgressionLevel() : ProgressionLevel.fromKey(progressionLevelId, this);
        this.isOffline = false;

        this.progressionLevel.startListening();
    }

    public static SinglePlayer fromFile(File playerDataFile, ServerPlayer player) {
        CompoundTag tag = null;
        try {
            tag = NbtIo.read(playerDataFile);
        }
        catch (IOException e) {
            CrashHandler.getInstance().handleException("Failed to read compound tag from" + playerDataFile.getName(), e, Level.ERROR);
        }
        return fromCompoundTag(tag, player);
    }

    public static SinglePlayer fromCompoundTag(CompoundTag tag, ServerPlayer player) {
        if(tag == null || tag.isEmpty()) return new SinglePlayer(player, createId(player.getStringUUID()));
        UUID uuid = tag.getUUID("uuid");
        String level = tag.getString("level");
        String team = tag.getString("team");
        if(player.getUUID().equals(uuid)) return new SinglePlayer(PlayerTeam.fromRegistry(team, player), player, createId(player.getStringUUID()), ResourceLocation.tryParse(level));
        throw new IllegalStateException("Uuid saved in player data doesn't match uuid of provided player! uuid found -> " + uuid + " | uuid provided -> " + player.getUUID());
    }


    public ServerPlayer getPlayer() {
        return player;
    }

    public Optional<PlayerTeam> getTeam() {
        return Optional.ofNullable(team);
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isInTeam(){
        return team != null;
    }

    public boolean isInTeam(PlayerTeam team){
        return team.equals(this.team);
    }

    public void updateProgressionLevel(ProgressionLevel level){
        this.progressionLevel = level;
        if(this.isInTeam()) this.team.updateProgressionLevel(this.progressionLevel, this);
        this.progressionLevel.startListening();
    }

    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }

    public boolean hasProgressionLevel(ProgressionLevel other){
        return this.progressionLevel.equals(other) || this.progressionLevel.contains(other);
    }

    public void toFile(File playerFile) {
        CompoundTag tag = this.toCompoundTag();
        try {
            NbtIo.write(tag, playerFile);
        }
        catch (IOException e) {
            CrashHandler.getInstance().handleException("Failed to write PlayerData to file! Affected player: " + playerName, e, Level.ERROR);
        }
    }

    private CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        tag.putString("level", this.progressionLevel.getId().toString());
        if(this.team != null) tag.putString("team", this.team.getId().toString());
        return tag;
    }

    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean isOffline() {
        return this.isOffline;
    }

    public void loggedOut() {
        this.isOffline = true;
    }

    public void setTeam(PlayerTeam playerTeam) {
        this.team = playerTeam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SinglePlayer player1 = (SinglePlayer) o;
        return isOffline == player1.isOffline && Objects.equal(playerName, player1.playerName) && Objects.equal(team, player1.team) && Objects.equal(uuid, player1.uuid) && Objects.equal(id, player1.id) && Objects.equal(player, player1.player) && Objects.equal(progressionLevel, player1.progressionLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(playerName, team, uuid, id, player, progressionLevel, isOffline);
    }
}
