package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.commands.ModCommand;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import de.thedead2.progression_reloaded.util.VersionManager;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.handler.FileHandler;
import de.thedead2.progression_reloaded.util.logger.MissingAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownRecipeCategoryFilter;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
    //TODO: extra class for event managing
    //TODO: Maybe better use Codecs for serialization and deserialization?
    //TODO: Level and quest builder classes --> + graphical ui of them
    //TODO: Use Gson and GsonHelper for whole project instead of json --> easier!
    //TODO: Instead of IForgeRegistry simple Maps for Levels and Quests? --> in game reload possible
    //TODO: Add custom Events

    public static final String MAIN_PACKAGE = ProgressionReloaded.class.getPackageName();

    public ProgressionReloaded(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        ModLoadingContext loadingContext = ModLoadingContext.get();

        this.registerTrigger(forgeEventBus);
        this.registerAbilities(forgeEventBus);
        this.registerRewards();
        this.registerPredicates();

        ModItems.register(modEventBus);

        if(isDevEnv()){
            ModRegistries.register(TestQuests.TEST1);
            ModRegistries.register(TestQuests.TEST2);
            ModRegistries.register(TestQuests.TEST3);
            ModRegistries.register(TestQuests.TEST4);
            ModRegistries.register(TestQuests.TEST5);

            ModRegistries.register(TestLevels.TEST1);
            ModRegistries.register(TestLevels.TEST2);
        }

        ModRegistries.register(TestLevels.CREATIVE);

        ModRegistries.register(modEventBus);


        modEventBus.addListener(this::setup);

        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        forgeEventBus.addListener(ModRegistries::onMissingMappings);
        forgeEventBus.addListener(this::onCommandsRegistration);
        forgeEventBus.addListener(this::onServerStarting);
        forgeEventBus.addListener(this::onServerStopping);
        forgeEventBus.addListener(this::onServerStopped);
        forgeEventBus.addListener(this::onPlayerFileLoad);
        forgeEventBus.addListener(this::onPlayerFileSave);
        forgeEventBus.addListener(this::onPlayerLoggedIn);
        forgeEventBus.addListener(this::onPlayerLoggedOut);
        forgeEventBus.addListener(this::onGameShuttingDown);
        forgeEventBus.addListener(this::onGameTick);
        forgeEventBus.addListener(LevelManager::onGameModeChange);

        forgeEventBus.register(this);
        registerLoggerFilter();
        VersionManager.register(modEventBus, forgeEventBus);
        //CrashHandler.getInstance().registerCrashListener(ModRegistries::saveRegistries);
    }

    private void registerAbilities(IEventBus forgeEventBus) {
        ReflectionHelper.registerClassesToEventBus(IAbility.class, forgeEventBus);
        DynamicRegistries.registerClasses(IAbility.class, DynamicRegistries.PROGRESSION_ABILITIES);
    }

    private void registerRewards(){
        DynamicRegistries.registerClasses(IReward.class, DynamicRegistries.PROGRESSION_REWARDS);
    }
    private void registerPredicates(){
        DynamicRegistries.registerClasses(ITriggerPredicate.class, DynamicRegistries.PROGRESSION_PREDICATES);
    }

    private void registerTrigger(IEventBus forgeEventBus) {
        ReflectionHelper.registerClassesToEventBus(SimpleTrigger.class, forgeEventBus);
        DynamicRegistries.registerClasses(SimpleTrigger.class, DynamicRegistries.PROGRESSION_TRIGGER);
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, MOD_VERSION);
        FileHandler.checkForMainDirectories();
        ModNetworkHandler.registerPackets();
    }

    private void onServerStarting(final ServerStartingEvent event){
        PlayerDataHandler.loadData(event.getServer().overworld());
        LevelManager.create();
    }

    private void onServerStopping(final ServerStoppingEvent event){
        LevelManager.getInstance().saveData();
        LevelManager.getInstance().getQuestManager().stopListening();
    }

    private void onServerStopped(final ServerStoppedEvent event){
        LevelManager.getInstance().reset();
    }

    private void onPlayerFileLoad(final PlayerEvent.LoadFromFile event){
        PlayerDataHandler.loadPlayerData(event.getPlayerFile(MOD_ID), event.getEntity());
        LevelManager.getInstance().updateData();
    }

    private void onPlayerFileSave(final PlayerEvent.SaveToFile event){
        PlayerDataHandler.savePlayerData(event.getEntity(), event.getPlayerFile(MOD_ID));
    }

    private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event){
        LevelManager.getInstance().checkForCreativeMode(PlayerDataHandler.getActivePlayer(event.getEntity()));
    }

    private void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event){
        PlayerDataHandler.getPlayerData().orElseThrow().playerLoggedOut(event.getEntity());
    }

    private void onCommandsRegistration(final RegisterCommandsEvent event) {
        ModCommand.registerCommands(event.getDispatcher());
    }

    private void onGameShuttingDown(final GameShuttingDownEvent event){
        //ModRegistries.saveRegistries();
    }

    private void onGameTick(final TickEvent.ServerTickEvent event){

    }

    private void registerLoggerFilter(){
        Logger rootLogger = LogManager.getRootLogger();

        if (rootLogger instanceof org.apache.logging.log4j.core.Logger logger) {
            logger.addFilter(new MissingAdvancementFilter());
            logger.addFilter(new UnknownRecipeCategoryFilter());
            logger.addFilter(new UnknownAdvancementFilter());
        }
        else {
            LOGGER.error("Unable to register filter for Logger with unexpected class: {}", rootLogger.getClass().getName());
        }
    }

    static {
        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }
}
