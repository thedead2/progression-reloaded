package de.thedead2.progression_reloaded.player.types;

import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.player.data.PlayerLevels;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.helper.PlayerHelper;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


public class PlayerData {
    private final String playerName;

    private final UUID uuid;

    private final Player player;

    private final PlayerLevels levels;

    private final PlayerQuests quests;

    @Nullable
    private PlayerTeam team;
    private int extraLives;


    public PlayerData(Player player) {
        this.playerName = player.getScoreboardName();
        this.team = null;
        this.uuid = player.getUUID();
        this.player = player;
        this.levels = new PlayerLevels(() -> this);
        this.quests = new PlayerQuests(() -> this);
        this.extraLives = 0;
    }


    public PlayerData(PlayerTeam team, Player player, PlayerLevels levels, PlayerQuests quests, int extraLives) {
        this(player.getScoreboardName(), team, player.getUUID(), player, levels, quests, extraLives);
    }


    public PlayerData(String playerName, @Nullable PlayerTeam team, UUID uuid, Player player, PlayerLevels levels, PlayerQuests quests, int extraLives) {
        this.playerName = playerName;
        this.team = team;
        this.uuid = uuid;
        this.player = player;
        this.levels = levels;
        this.quests = quests;
        this.extraLives = extraLives;
    }


    public static PlayerData loadFromFile(File playerDataFile, ServerPlayer player) {
        CompoundTag tag = null;
        try {
            if(playerDataFile.exists()) {
                tag = NbtIo.readCompressed(playerDataFile);
            }
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to read compound tag from" + playerDataFile.getName(), e, Level.FATAL);
        }
        return loadFromNBT(tag, player);
    }


    public static PlayerData loadFromNBT(CompoundTag tag, ServerPlayer player) {
        if(tag == null || tag.isEmpty()) {
            return new PlayerData(player);
        }
        UUID uuid = tag.getUUID("uuid");

        if(!player.getUUID().equals(uuid)) {
            throw new IllegalStateException("Uuid saved in player data doesn't match uuid of provided player! uuid found -> " + uuid + " | uuid provided -> " + player.getUUID());
        }

        PlayerTeam team = PlayerTeam.fromRegistry(tag.getString("team"), player);
        PlayerLevels levels = PlayerLevels.loadFromNBT(tag.getCompound("levels"));
        PlayerQuests quests = PlayerQuests.loadFromNBT(tag.getCompound("quests"));
        int extraLives = tag.getInt("extra_lives");

        return new PlayerData(team, player, levels, quests, extraLives);
    }


    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name);
    }


    public static PlayerData fromNetwork(FriendlyByteBuf buf) {
        String playerName = buf.readUtf();
        UUID uuid = buf.readUUID();
        Player player = PlayerHelper.getPlayerForUUID(uuid);
        PlayerTeam team = buf.readNullable(PlayerTeam::fromNetwork);
        PlayerLevels levels = PlayerLevels.loadFromNetwork(buf);
        PlayerQuests quests = PlayerQuests.loadFromNetwork(buf);
        int extraLives = buf.readInt();

        return new PlayerData(playerName, team, uuid, player, levels, quests, extraLives);
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


    public String getName() {
        return playerName;
    }


    public UUID getUUID() {
        return uuid;
    }


    public boolean isInTeam(PlayerTeam team) {
        return team.equals(this.team);
    }


    public void updateProgressionLevel(ProgressionLevel level) {
        this.levels.updateLevel(level);
    }


    public boolean hasProgressionLevel(ProgressionLevel other) {
        ProgressionLevel currentLevel = this.getCurrentLevel();
        return currentLevel.equals(other) || currentLevel.contains(other);
    }


    public boolean hasProgressionLevel(ResourceLocation other) {
        return this.hasProgressionLevel(ModRegistries.LEVELS.get().getValue(other));
    }


    public ProgressionLevel getCurrentLevel() {
        return levels.getCurrentLevel();
    }


    public void setTeam(PlayerTeam playerTeam) {
        this.team = playerTeam;
        if(this.player instanceof ServerPlayer serverPlayer) {
            ModNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(this), serverPlayer);
        }
    }


    public void saveToFile(File playerFile) {
        CompoundTag tag = this.saveToNBT();
        try {
            NbtIo.writeCompressed(tag, playerFile);
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to write PlayerData to file! Affected player: " + playerName, e, Level.FATAL);
        }
    }


    private CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        if(this.team != null) {
            tag.putString("team", this.team.getId().toString());
        }
        tag.put("levels", this.levels.saveToNBT());
        tag.put("quests", this.quests.saveToNBT());
        tag.putInt("extra_lives", this.extraLives);

        return tag;
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


    public boolean hasAndUpdateExtraLives() {
        if(this.extraLives > 0) {
            this.extraLives--;
            return true;
        }
        return false;
    }


    public int getExtraLives() {
        return this.extraLives;
    }



    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.playerName);
        buf.writeUUID(this.uuid);
        buf.writeNullable(this.team, (buf1, team1) -> team1.toNetwork(buf1));
        this.levels.saveToNetwork(buf);
        this.quests.saveToNetwork(buf);
        buf.writeInt(this.extraLives);
    }


    public PlayerLevels getLevelData() {
        return this.levels;
    }


    public PlayerQuests getQuestData() {
        return this.quests;
    }


    public LevelProgress getCurrentLevelProgress() {
        return this.levels.getCurrentLevelProgress();
    }


    public void copyQuestProgress(PlayerQuests playerQuests) {
        this.quests.copy(playerQuests);
    }
}
