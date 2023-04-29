package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.commands.ModCommand;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModRegistries;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static de.thedead2.progression_reloaded.util.ModHelper.*;

@Mod(MOD_ID)
public class ProgressionReloaded {

    public static final String MAIN_PACKAGE = ProgressionReloaded.class.getPackageName();

    public ProgressionReloaded(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModRegistries.register(modEventBus);
        modEventBus.addListener(this::setup);

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");


        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.addListener(this::onCommandsRegistration);
        forgeEventBus.addListener(this::onServerStarting);
        forgeEventBus.addListener(this::onPlayerFileLoad);
        forgeEventBus.addListener(this::onPlayerFileSave);
        forgeEventBus.addListener(this::onPlayerLoggedOut);
        forgeEventBus.addListener(this::onGameShuttingDown);
        forgeEventBus.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, MOD_VERSION);
        ModNetworkHandler.registerPackets();
    }

    private void onServerStarting(final ServerStartingEvent event){
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        PlayerDataHandler.loadData(level);
    }

    private void onPlayerFileLoad(PlayerEvent.LoadFromFile event){
        PlayerDataHandler.loadPlayerData(event.getPlayerFile(MOD_ID), event.getEntity());
    }

    private void onPlayerFileSave(PlayerEvent.SaveToFile event){
        PlayerDataHandler.savePlayerData(event.getEntity(), event.getPlayerFile(MOD_ID));
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event){
        PlayerDataHandler.getPlayerData().orElseThrow().playerLoggedOut(event.getEntity());
    }

    private void onCommandsRegistration(final RegisterCommandsEvent event) {
        ModCommand.registerCommands(event.getDispatcher());
    }

    private void onGameShuttingDown(final GameShuttingDownEvent event){
        TranslationKeyProvider.saveKeys();
    }

    static {
        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }
}
