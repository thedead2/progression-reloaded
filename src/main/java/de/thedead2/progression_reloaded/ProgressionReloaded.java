package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.client.PRKeyMappings;
import de.thedead2.progression_reloaded.commands.ModCommands;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.RestrictionManager;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.loot.ModLootModifiers;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.GameState;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import de.thedead2.progression_reloaded.util.VersionManager;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.handler.FileHandler;
import de.thedead2.progression_reloaded.util.logger.MissingAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownFontTypeFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownRecipeCategoryFilter;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import de.thedead2.progression_reloaded.util.registries.TypeRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.progression_reloaded.util.ModHelper.*;


@Mod(MOD_ID)
public class ProgressionReloaded {
    //TODO: Maybe better use Codecs for serialization and deserialization?
    //TODO: Level and quest builder classes --> + graphical ui of them
    //TODO: Use Gson and GsonHelper for whole project instead of json --> easier!
    //TODO: Instead of IForgeRegistry simple Maps for Levels and Quests? --> in game reload possible

    public static final String MAIN_PACKAGE = ProgressionReloaded.class.getPackageName();


    static {
        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }


    public ProgressionReloaded() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        ModLoadingContext loadingContext = ModLoadingContext.get();

        this.registerTrigger(forgeEventBus);
        this.registerRewards();

        ModItems.register(modEventBus);

        TestQuests.register();
        TestLevels.register();
        ModRegistries.register(LevelManager.CREATIVE);

        ModRegistries.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(PRKeyMappings::register);

        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        forgeEventBus.addListener(ModRegistries::onMissingMappings);

        forgeEventBus.register(this);
        forgeEventBus.register(RestrictionManager.class);

        registerLoggerFilter();
        VersionManager.register(modEventBus, forgeEventBus);
        //CrashHandler.getInstance().registerCrashListener(ModRegistries::saveRegistries);
    }


    private void registerTrigger(IEventBus forgeEventBus) {
        ReflectionHelper.registerClassesToEventBus(SimpleCriterionTrigger.class, forgeEventBus);
        TypeRegistries.registerClasses(SimpleCriterionTrigger.class, TypeRegistries.PROGRESSION_TRIGGER);
    }


    private void registerRewards() {
        TypeRegistries.registerClasses(IReward.class, TypeRegistries.PROGRESSION_REWARDS);
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, MOD_VERSION);
        FileHandler.checkForMainDirectories();
        PRNetworkHandler.registerPackets();
    }


    @SubscribeEvent
    public void onCommandsRegistration(final RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher());
    }


    @SubscribeEvent
    public void onServerAboutToStart(final ServerAboutToStartEvent event) {
        GAME_STATE = GameState.ABOUT_TO_START;
        LevelManager.create();
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        PlayerDataManager.loadData(event.getServer().overworld());
    }


    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent event) {
        GAME_STATE = GameState.ABOUT_TO_STOP;
    }


    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        LevelManager.getInstance().reset();
        GAME_STATE = GameState.INACTIVE;
    }


    @SubscribeEvent
    public void onGameShuttingDown(final GameShuttingDownEvent event) {
        //ModRegistries.saveRegistries();
    }


    @SubscribeEvent
    public void onGameTick(final TickEvent.ServerTickEvent event) {

    }


    private void registerLoggerFilter() {
        Logger rootLogger = LogManager.getRootLogger();

        if(rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
            logger.addFilter(new UnknownAdvancementFilter());
            logger.addFilter(new UnknownFontTypeFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with unexpected class: {}", rootLogger.getClass().getName());
        }
    }
}
