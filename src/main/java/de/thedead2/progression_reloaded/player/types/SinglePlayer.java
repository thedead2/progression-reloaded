package de.thedead2.progression_reloaded.player.types;

import com.google.common.base.Objects;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class SinglePlayer {

    private final String playerName;

    private final UUID uuid;

    private final ResourceLocation id;

    private final ServerPlayer player;

    private final Map<ResourceLocation, IAbility<?>> playerAbilities = new HashMap<>();

    private PlayerTeam team;

    private ProgressionLevel progressionLevel;

    private boolean isOffline;


    public SinglePlayer(ServerPlayer player, ResourceLocation id) {
        this(
                null,
                player,
                id,
                player.gameMode.isCreative() && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() ? TestLevels.CREATIVE.getId() : ResourceLocationHelper.getOrDefault(
                        ConfigManager.DEFAULT_STARTING_LEVEL.get(),
                        TestLevels.CREATIVE.getId()
                )
        );
    }


    public SinglePlayer(PlayerTeam team, ServerPlayer player, ResourceLocation id, ResourceLocation progressionLevelId) {
        this.playerName = player.getScoreboardName();
        this.team = team;
        this.uuid = player.getUUID();
        this.id = id;
        this.player = player;
        this.progressionLevel = this.team != null ? this.team.getProgressionLevel() : ProgressionLevel.fromKey(progressionLevelId);
        this.isOffline = false;
    }


    public static SinglePlayer fromFile(File playerDataFile, ServerPlayer player) {
        CompoundTag tag = null;
        try {
            tag = NbtIo.read(playerDataFile);
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException(
                    "Failed to read compound tag from" + playerDataFile.getName(),
                    e,
                    Level.FATAL
            );
        }
        return fromCompoundTag(tag, player);
    }


    public static SinglePlayer fromCompoundTag(CompoundTag tag, ServerPlayer player) {
        if(tag == null || tag.isEmpty()) {
            return new SinglePlayer(player, createId(player.getStringUUID()));
        }
        UUID uuid = tag.getUUID("uuid");
        String level = tag.getString("level");
        String team = tag.getString("team");
        if(player.getUUID().equals(uuid)) {
            return new SinglePlayer(PlayerTeam.fromRegistry(team, player), player, createId(player.getStringUUID()), ResourceLocation.tryParse(level));
        }
        throw new IllegalStateException("Uuid saved in player data doesn't match uuid of provided player! uuid found -> " + uuid + " | uuid provided -> " + player.getUUID());
    }


    public static ResourceLocation createId(String name) {
        return ResourceLocation.tryBuild(ModHelper.MOD_ID, name);
    }


    public ServerPlayer getServerPlayer() {
        return player;
    }


    public Optional<PlayerTeam> getTeam() {
        return Optional.ofNullable(team);
    }


    public void setTeam(PlayerTeam playerTeam) {
        this.team = playerTeam;
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


    public void toFile(File playerFile) {
        CompoundTag tag = this.toCompoundTag();
        try {
            NbtIo.write(tag, playerFile);
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to write PlayerData to file! Affected player: " + playerName, e, Level.FATAL);
        }
    }


    private CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        tag.putString("level", this.progressionLevel.getId().toString());
        if(this.team != null) {
            tag.putString("team", this.team.getId().toString());
        }
        return tag;
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


    @Override
    public int hashCode() {
        return Objects.hashCode(playerName, team, uuid, id, player, progressionLevel, playerAbilities, isOffline);
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        SinglePlayer player1 = (SinglePlayer) o;
        return isOffline == player1.isOffline
                && Objects.equal(playerName, player1.playerName)
                && Objects.equal(team, player1.team) && Objects.equal(uuid, player1.uuid)
                && Objects.equal(id, player1.id) && Objects.equal(player, player1.player)
                && Objects.equal(progressionLevel, player1.progressionLevel)
                && Objects.equal(playerAbilities, player1.playerAbilities);
    }


    public void addAbilities(Collection<IAbility<?>> abilities) {
        abilities.forEach(this::addAbility);
    }


    public void addAbility(IAbility<?> ability) {
        if(this.isInTeam() && !this.team.hasAbility(ability)) {
            this.team.addAbility(ability);
        }
        else {
            this.playerAbilities.put(ability.getId(), ability);
        }
    }


    public boolean isInTeam() {
        return team != null;
    }


    public void removeAbilities(Collection<IAbility<?>> abilities) {
        abilities.forEach(this::removeAbility);
    }


    public void removeAbility(IAbility<?> ability) {
        if(this.isInTeam() && this.team.hasAbility(ability)) {
            this.team.removeAbility(ability);
        }
        else {
            this.playerAbilities.remove(ability.getId());
        }
    }


    public <T> IAbility<T> getAbility(Class<? extends IAbility<T>> abilityClass) {
        AtomicReference<IAbility<T>> ability = new AtomicReference<>();
        this.playerAbilities.forEach((resourceLocation, iAbility) -> {
            if(iAbility.getClass().getName().equals(abilityClass.getName())) {
                ability.set((IAbility<T>) iAbility);
            }
        });
        return ability.get();
    }
}
