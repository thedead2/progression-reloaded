package de.thedead2.progression_reloaded.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerData extends SavedData {
    private final Set<ResourceLocation> knownPlayers = new HashSet<>();
    private final Map<ResourceLocation, SinglePlayer> activePlayers = new HashMap<>();

    public PlayerData(Collection<ResourceLocation> knownPlayers){
        this.knownPlayers.addAll(knownPlayers);
        this.setDirty();
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        AtomicInteger i = new AtomicInteger();
        knownPlayers.forEach(resourceLocation -> tag.putInt(resourceLocation.toString(), i.getAndIncrement()));
        return tag;
    }

    public static PlayerData load(CompoundTag tag) {
        Set<ResourceLocation> knownPlayers = new HashSet<>();
        tag.getAllKeys().forEach(s -> knownPlayers.add(ResourceLocation.tryParse(s)));
        return new PlayerData(knownPlayers);
    }

    public void addActivePlayer(SinglePlayer singlePlayer){
        this.activePlayers.put(singlePlayer.getId(), singlePlayer);
        this.addKnownPlayer(singlePlayer.getPlayer());
    }

    public SinglePlayer getActivePlayer(ResourceLocation id){
        return this.activePlayers.get(id);
    }

    public SinglePlayer getActivePlayer(Player player){
        return getActivePlayer(SinglePlayer.createId(player.getStringUUID()));
    }

    public Collection<SinglePlayer> allPlayerData() {
        return this.activePlayers.values();
    }

    public void removePlayerFromActive(ResourceLocation id){
        this.activePlayers.remove(id);
    }

    public void removePlayerFromActive(Player player){
        removePlayerFromActive(SinglePlayer.createId(player.getStringUUID()));
    }

    public void addActivePlayer(Player player, File playerFile) {
        this.addActivePlayer(SinglePlayer.fromFile(playerFile, player));
    }

    public void addKnownPlayer(Player player){
        if(this.knownPlayers.add(SinglePlayer.createId(player.getStringUUID()))) this.setDirty();
    }
}
