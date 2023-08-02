package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.commands.ModCommand;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.level.LevelManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.*;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.resources.ResourceLocation;
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

import static de.thedead2.progression_reloaded.util.ModHelper.*;

@Mod(MOD_ID)
public class ProgressionReloaded {
    //TODO: extra class for event managing
    //TODO: Maybe better use Codecs for serialization and deserialization?
    //TODO: Level and quest builder classes --> + graphical ui of them

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
        ModRegistries.DeferredRegisters.register(modEventBus);

        ModRegistries.register(ProgressionQuest.Test());
        ModRegistries.register(ProgressionQuest.Test2());
        ModRegistries.register(ProgressionQuest.Test3());
        ModRegistries.register(ProgressionQuest.Test4());
        ModRegistries.register(ProgressionLevel.CREATIVE);
        ModRegistries.register(ProgressionLevel.TEST);
        ModRegistries.register(ProgressionLevel.TEST2);


        modEventBus.addListener(this::setup);

        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        forgeEventBus.addListener(ModRegistries::onMissingMappings);
        forgeEventBus.addListener(this::onCommandsRegistration);
        forgeEventBus.addListener(this::onServerStarting);
        forgeEventBus.addListener(this::onServerStopping);
        forgeEventBus.addListener(this::onServerStopped);
        forgeEventBus.addListener(this::onPlayerFileLoad);
        forgeEventBus.addListener(this::onPlayerFileSave);
        forgeEventBus.addListener(this::onPlayerLoggedOut);
        forgeEventBus.addListener(this::onGameShuttingDown);
        forgeEventBus.addListener(this::onGameTick);

        forgeEventBus.register(this);
        VersionManager.register(modEventBus, forgeEventBus);
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
        ModNetworkHandler.registerPackets();
    }

    private void onServerStarting(final ServerStartingEvent event){
        PlayerDataHandler.loadData(event.getServer().overworld());
        LevelManager.create();
    }

    private void onServerStopping(final ServerStoppingEvent event){
        LevelManager.getInstance().saveData();
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

    static {
        CrashReportCallables.registerCrashCallable(CrashHandler.getInstance());
    }
}
