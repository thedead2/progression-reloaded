package de.thedead2.progression_reloaded.util;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.targets.CommonDevLaunchHandler;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;


public abstract class ModHelper {

    public static final String MOD_ID = "progression_reloaded";

    public static final String MOD_UPDATE_LINK = "https://www.curseforge.com/minecraft/mc-mods/progression-reloaded";

    public static final String MOD_ISSUES_LINK = "https://github.com/thedead2/progression-reloaded/issues";

    public static final Path GAME_DIR = FMLPaths.GAMEDIR.get();

    public static final char PATH_SEPARATOR = File.separatorChar;

    public static final Path DIR_PATH = GAME_DIR.resolve(MOD_ID);

    public static final Path LEVELS_PATH = DIR_PATH.resolve("levels");

    public static final Path QUESTS_PATH = DIR_PATH.resolve("quests");

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final ModProperties MOD_PROPERTIES = ModProperties.fromInputStream(ReflectionHelper.findResource("META-INF/mod.properties"));

    public static final String MOD_VERSION = MOD_PROPERTIES.getProperty("mod_version");

    public static final String MOD_NAME = MOD_PROPERTIES.getProperty("mod_name");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    public static GameState GAME_STATE = GameState.INACTIVE;


    public static IModFile THIS_MOD_FILE() {
        return ModList.get().getModFileById(MOD_ID).getFile();
    }


    public static ModContainer THIS_MOD_CONTAINER() {
        return ModList.get().getModContainerById(MOD_ID).orElseThrow(() -> new RuntimeException("Unable to retrieve ModContainer for id: " + MOD_ID));
    }


    public static boolean isDevEnv() {
        return FMLLoader.getLaunchHandler() instanceof CommonDevLaunchHandler;
    }


    public static boolean isRunningOnServerThread() {
        return EffectiveSide.get().isServer();
    }


    public static boolean isRunningOnPhysicalServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }


    public static void reloadAll(MinecraftServer server) {
        Timer timer = new Timer();
        LOGGER.info("Reloading...");

        init();
        reloadGameData(server);

        LOGGER.info("Reload completed in {} ms!", timer.getTime());
        timer.stop();
    }


    public static void init() {

    }


    private static void reloadGameData(MinecraftServer server) {
        PackRepository packRepository = server.getPackRepository();
        WorldData worldData = server.getWorldData();

        packRepository.reload();
        Collection<String> selectedIds = Lists.newArrayList(packRepository.getSelectedIds());
        Collection<String> disabledPacks = worldData.getDataConfiguration().dataPacks().getDisabled();

        for(String ids : packRepository.getAvailableIds()) {
            if(!disabledPacks.contains(ids) && !selectedIds.contains(ids)) {
                selectedIds.add(ids);
            }
        }

        server.reloadResources(selectedIds).exceptionally((e) -> {
            server.sendSystemMessage(TranslationKeyProvider.chatMessage("reload_failed_message", ChatFormatting.RED));
            CrashHandler.getInstance().handleException("Failed to execute reload!", e, Level.ERROR);
            return null;
        });
    }

}
