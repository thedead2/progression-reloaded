package de.thedead2.progression_reloaded.player.types;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerPacket;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.helper.PlayerHelper;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


public class PlayerData {

    private final String playerName;

    private final UUID uuid;

    private final ResourceLocation id;

    private final Player player;

    private PlayerTeam team;

    private ProgressionLevel progressionLevel;

    private int extraLives;


    public PlayerData(Player player, ResourceLocation id) {
        this(null, player, id, player instanceof ServerPlayer serverPlayer ? serverPlayer.gameMode.isCreative() && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() ? TestLevels.CREATIVE.getId() :
                ResourceLocationHelper.getOrDefault(ConfigManager.DEFAULT_STARTING_LEVEL.get(), TestLevels.CREATIVE.getId()) : TestLevels.CREATIVE.getId(), 0);
    }


    public PlayerData(PlayerTeam team, Player player, ResourceLocation id, ResourceLocation progressionLevelId, int extraLives) {
        this(player.getScoreboardName(), team, player.getUUID(), player, id, progressionLevelId, extraLives);
    }


    public PlayerData(String playerName, PlayerTeam team, UUID uuid, Player player, ResourceLocation id, ResourceLocation progressionLevelId, int extraLives) {
        this.playerName = playerName;
        this.team = team;
        this.uuid = uuid;
        this.id = id;
        this.player = player;
        this.progressionLevel = this.team != null ? this.team.getProgressionLevel() : ProgressionLevel.fromKey(progressionLevelId);
        this.extraLives = extraLives;
    }


    public static PlayerData fromFile(File playerDataFile, ServerPlayer player) {
        CompoundTag tag = null;
        try {
            if(playerDataFile.exists()) {
                tag = NbtIo.readCompressed(playerDataFile);
            }
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to read compound tag from" + playerDataFile.getName(), e, Level.FATAL);
        }
        return fromCompoundTag(tag, player);
    }


    public static PlayerData fromCompoundTag(CompoundTag tag, ServerPlayer player) {
        if(tag == null || tag.isEmpty()) {
            return new PlayerData(player, createId(player.getStringUUID()));
        }
        UUID uuid = tag.getUUID("uuid");
        String level = tag.getString("level");
        String team = tag.getString("team");
        int extraLives = tag.getInt("extra_lives");
        if(player.getUUID().equals(uuid)) {
            return new PlayerData(PlayerTeam.fromRegistry(team, player), player, createId(player.getStringUUID()), ResourceLocation.tryParse(level), extraLives);
        }
        throw new IllegalStateException("Uuid saved in player data doesn't match uuid of provided player! uuid found -> " + uuid + " | uuid provided -> " + player.getUUID());
    }


    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name);
    }


    public static PlayerData fromNetwork(FriendlyByteBuf buf) {
        String playerName = buf.readUtf();
        UUID uuid = buf.readUUID();
        ResourceLocation id = buf.readResourceLocation();
        PlayerTeam team = buf.readNullable(PlayerTeam::fromNetwork);
        ResourceLocation levelId = buf.readResourceLocation();
        int extraLives = buf.readInt();
        Player player = PlayerHelper.getPlayerForUUID(uuid);
        return new PlayerData(playerName, team, uuid, player, id, levelId, extraLives);
    }


    public ServerPlayer getServerPlayer() {
        if(this.player instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        else {
            throw new UnsupportedOperationException("Tried to access ServerPlayer but it's a LocalPlayer!");
        }
    }


    public Optional<PlayerTeam> getTeam() {
        return Optional.ofNullable(team);
    }


    public Player getPlayer() {
        return this.player;
    }


    public String getPlayerName() {
        return playerName;
    }


    public UUID getUuid() {
        return uuid;
    }


    public boolean isInTeam(PlayerTeam team) {
        return team.equals(this.team);
    }


    public void updateProgressionLevel(ProgressionLevel level) {
        this.progressionLevel = level;
    }


    public ProgressionLevel getProgressionLevel() {
        return progressionLevel;
    }


    public boolean hasProgressionLevel(ResourceLocation other) {
        return this.hasProgressionLevel(ModRegistries.LEVELS.get().getValue(other));
    }


    public boolean hasProgressionLevel(ProgressionLevel other) {
        return this.progressionLevel.equals(other) || this.progressionLevel.contains(other);
    }


    public void setTeam(PlayerTeam playerTeam) {
        this.team = playerTeam;
        if(this.player instanceof ServerPlayer serverPlayer) {
            ModNetworkHandler.sendToPlayer(new ClientSyncPlayerPacket(this), serverPlayer);
        }
    }


    private CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        tag.putString("level", this.progressionLevel.getId().toString());
        if(this.team != null) {
            tag.putString("team", this.team.getId().toString());
        }
        tag.putInt("extra_lives", this.extraLives);
        return tag;
    }


    public ResourceLocation getId() {
        return this.id;
    }


    public void toFile(File playerFile) {
        CompoundTag tag = this.toCompoundTag();
        try {
            NbtIo.writeCompressed(tag, playerFile);
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to write PlayerData to file! Affected player: " + playerName, e, Level.FATAL);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(playerName, team, uuid, id, player, progressionLevel);
    }





    public boolean isInTeam() {
        return team != null;
    }


    public boolean addExtraLife() {
        if(ConfigManager.MAX_EXTRA_LIVES.get() > this.extraLives) {
            this.extraLives++;
            this.player.sendSystemMessage(Component.literal("Congratulations " + playerName + "! You earned an extra life!").withStyle(ChatFormatting.LIGHT_PURPLE));
            return true;
        }
        return false;
    }


    public boolean hasExtraLife() {
        if(this.extraLives > 0) {
            this.extraLives--;
            return true;
        }
        return false;
    }


    public int getExtraLives() {
        return this.extraLives;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerData player1 = (PlayerData) o;
        return Objects.equal(playerName, player1.playerName)
                && Objects.equal(team, player1.team) && Objects.equal(uuid, player1.uuid)
                && Objects.equal(id, player1.id) && Objects.equal(player, player1.player)
                && Objects.equal(progressionLevel, player1.progressionLevel);
    }


    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.playerName);
        buf.writeUUID(this.uuid);
        buf.writeResourceLocation(this.id);
        buf.writeNullable(this.team, (buf1, team1) -> team1.toNetwork(buf1));
        buf.writeResourceLocation(this.progressionLevel.getId());
        buf.writeInt(this.extraLives);
    }
}
