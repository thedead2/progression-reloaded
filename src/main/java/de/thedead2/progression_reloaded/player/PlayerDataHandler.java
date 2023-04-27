package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.ModRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.registries.DeferredRegister;

import java.io.File;

public abstract class PlayerDataHandler {

    private static final DeferredRegister<PlayerData> DATA = DeferredRegister.create(ModRegistries.Keys.PLAYER_DATA, ModHelper.MOD_ID);

    public static void loadPlayerData(File playerDataFile, Player player){
        DATA.register(player.getStringUUID(), () -> PlayerData.fromFile(playerDataFile, player));
    }

    public static void loadTeamData(DimensionDataStorage dataStorage){
        dataStorage.computeIfAbsent(TeamData::load, TeamData::new, "teams");
    }

    public static void savePlayerData(Player player, File playerFile) {
        ModRegistries.PROGRESSION_PLAYER_DATA.get().getValue(new ResourceLocation(ModHelper.MOD_ID, player.getStringUUID())).toFile(playerFile);
    }

    public static void saveTeamData(){

    }
}
