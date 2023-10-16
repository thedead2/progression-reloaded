package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.commands.ModCommands;
import de.thedead2.progression_reloaded.data.AbilityManager;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.abilities.ModRestrictionManagers;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.events.RegisterEvent;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.loot.ModLootModifiers;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerPacket;
import de.thedead2.progression_reloaded.network.packets.ClientUsedExtraLifePacket;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.PlayerData;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.thedead2.progression_reloaded.util.ModHelper.*;
import static de.thedead2.progression_reloaded.util.helper.MathHelper.secondsToTicks;


@Mod(MOD_ID)
public class ProgressionReloaded {
    //TODO: extra class for event managing
    //TODO: Maybe better use Codecs for serialization and deserialization?
    //TODO: Level and quest builder classes --> + graphical ui of them
    //TODO: Use Gson and GsonHelper for whole project instead of json --> easier!
    //TODO: Instead of IForgeRegistry simple Maps for Levels and Quests? --> in game reload possible
    //TODO: Add custom Events

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
        this.registerPredicates();

        ModItems.register(modEventBus);

        if(isDevEnv()) {
            ModRegistries.register(TestQuests.TEST1);
            ModRegistries.register(TestQuests.TEST2);
            ModRegistries.register(TestQuests.TEST3);
            ModRegistries.register(TestQuests.TEST4);
            ModRegistries.register(TestQuests.TEST5);
            ModRegistries.register(TestQuests.TEST6);
            ModRegistries.register(TestQuests.TEST7);
            ModRegistries.register(TestQuests.TEST8);

            ModRegistries.register(TestLevels.TEST1);
            ModRegistries.register(TestLevels.TEST2);
        }

        ModRegistries.register(LevelManager.CREATIVE);

        ModRegistries.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModRestrictionManagers.register();

        modEventBus.addListener(this::setup);

        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        forgeEventBus.addListener(ModRegistries::onMissingMappings);
        forgeEventBus.addListener(LevelManager::onGameModeChange);

        forgeEventBus.register(this);
        forgeEventBus.register(AbilityManager.class);
        if(FMLEnvironment.dist.isClient()) {
            forgeEventBus.register(ModClientInstance.getInstance());
        }

        registerLoggerFilter();
        VersionManager.register(modEventBus, forgeEventBus);
        //CrashHandler.getInstance().registerCrashListener(ModRegistries::saveRegistries);
    }


    private void registerTrigger(IEventBus forgeEventBus) {
        ReflectionHelper.registerClassesToEventBus(SimpleTrigger.class, forgeEventBus);
        TypeRegistries.registerClasses(SimpleTrigger.class, TypeRegistries.PROGRESSION_TRIGGER);
    }


    private void registerRewards() {
        TypeRegistries.registerClasses(IReward.class, TypeRegistries.PROGRESSION_REWARDS);
    }


    private void registerPredicates() {
        TypeRegistries.registerClasses(ITriggerPredicate.class, TypeRegistries.PROGRESSION_PREDICATES);
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Starting {}, Version: {}", MOD_NAME, MOD_VERSION);
        FileHandler.checkForMainDirectories();
        ModNetworkHandler.registerPackets();
    }


    @SubscribeEvent
    public void onCommandsRegistration(final RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher());
    }


    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        GAME_STATE = GameState.ABOUT_TO_START;
        PlayerDataHandler.loadData(event.getServer().overworld());
        LevelManager.create(event.getServer().overworld());
    }


    @SubscribeEvent
    public void onServerStopping(final ServerStoppingEvent event) {
        GAME_STATE = GameState.ABOUT_TO_STOP;
        LevelManager.getInstance().saveData();
        LevelManager.getInstance().getQuestManager().stopListening();
    }


    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        LevelManager.getInstance().reset();
        GAME_STATE = GameState.INACTIVE;
    }


    @SubscribeEvent
    public void onPlayerFileLoad(final PlayerEvent.LoadFromFile event) {
        PlayerDataHandler.loadPlayerData(event.getPlayerFile(MOD_ID), event.getEntity());
        LevelManager.getInstance().updateData();
    }


    @SubscribeEvent
    public void onPlayerFileSave(final PlayerEvent.SaveToFile event) {
        PlayerDataHandler.savePlayerData(event.getEntity(), event.getPlayerFile(MOD_ID));
        PlayerData data = PlayerDataHandler.getActivePlayer(event.getEntity());
        ModNetworkHandler.sendToPlayer(new ClientSyncPlayerPacket(data), data.getServerPlayer());

        if(GAME_STATE == GameState.PLAYER_LOGGED_OUT) {
            Player player = event.getEntity();
            PlayerDataHandler.removeActivePlayer(player);
            if(player instanceof ServerPlayer serverPlayer) {
                AbilityManager.syncRestrictionsWithClient(serverPlayer, true);
            }
        }
    }


    //We're only hooking in when the player is being placed in the world, the connection is established and the UpdateRecipesEvent hasn't fired yet
    @SubscribeEvent
    public void onDataPackReload(final OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if(player != null) {
            PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
            ModNetworkHandler.sendToPlayer(new ClientSyncPlayerPacket(playerData), player);
            AbilityManager.syncRestrictionsWithClient(player, false);
        }
    }


    @SubscribeEvent
    public void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        GAME_STATE = GameState.PLAYER_LOGGED_IN;
        Player player = event.getEntity();
        PlayerData playerData = PlayerDataHandler.getActivePlayer(player);
        LevelManager.getInstance().checkForCreativeMode(playerData);
        LevelManager.getInstance().updateStatus();
    }


    @SubscribeEvent
    public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        GAME_STATE = GameState.PLAYER_LOGGED_OUT;
    }


    @SubscribeEvent
    public void onGameShuttingDown(final GameShuttingDownEvent event) {
        //ModRegistries.saveRegistries();
    }


    @SubscribeEvent
    public void onGameTick(final TickEvent.ServerTickEvent event) {

    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.level.isClientSide()) {
            PlayerData player = PlayerDataHandler.getActivePlayer(serverPlayer);
            if(player.hasExtraLife() || ExtraLifeItem.isUnlimited()) {
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
                serverPlayer.removeAllEffects();
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(30), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, secondsToTicks(15), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.SATURATION, secondsToTicks(25), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, secondsToTicks(15), 0));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.CONFUSION, secondsToTicks(7)));
                event.setCanceled(true);
                ModNetworkHandler.sendToPlayer(new ClientUsedExtraLifePacket(serverPlayer), serverPlayer);
                ModNetworkHandler.sendToPlayer(new ClientSyncPlayerPacket(player), serverPlayer);
            }
        }
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
