package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.data.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

public class SinglePlayer {

    private final String playerName;

    private final PlayerTeam team;

    private final UUID uuid;
    private final ResourceLocation id;

    private final Player player;

    private ProgressionLevel progressionLevel;

    public SinglePlayer(Player player, ResourceLocation id, ProgressionLevel progressionLevel) {
        this(null, player, id, progressionLevel);
    }

    public SinglePlayer(PlayerTeam team, Player player, ResourceLocation id, ProgressionLevel progressionLevel) {
        this.playerName = player.getScoreboardName();
        this.team = team;
        this.uuid = player.getUUID();
        this.id = id;
        this.player = player;
        this.progressionLevel = progressionLevel;
    }

    public static SinglePlayer fromFile(File playerDataFile, net.minecraft.world.entity.player.Player player) {
        CompoundTag tag = null;
        try {
            tag = NbtIo.read(playerDataFile);
        }
        catch (IOException e) {
            CrashHandler.getInstance().handleException("Failed to read compound tag from" + playerDataFile.getName(), e, Level.ERROR, true);
        }
        return fromCompoundTag(tag, player);
    }

    public static SinglePlayer fromCompoundTag(CompoundTag tag, Player player) {
        //if() throw new NullPointerException("Can't create PlayerData with compound tag that is null!");
        if(tag == null || tag.isEmpty()) return new SinglePlayer(player, createId(player.getStringUUID()), ProgressionLevel.lowest());
        UUID uuid = tag.getUUID("uuid");
        String level = tag.getString("level");
        String team = tag.getString("team");
        if(player.getUUID().equals(uuid)) return new SinglePlayer(PlayerTeam.fromRegistry(ResourceLocation.tryParse(team)), player, createId(player.getStringUUID()), ProgressionLevel.fromKey(ResourceLocation.tryParse(level)));
        throw new IllegalStateException("Uuid saved in player data doesn't match uuid of provided player! uuid found -> " + uuid + " | uuid provided -> " + player.getUUID());
    }


    public net.minecraft.world.entity.player.Player getPlayer() {
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
            CrashHandler.getInstance().handleException("Failed to write PlayerData to file! Affected player: " + playerName, e, Level.ERROR, true);
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
}
