package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.commands.ModCommand;
import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.*;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.*;
import de.thedead2.progression_reloaded.data.trigger.KillTrigger;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.data.trigger.SleepTrigger;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.*;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.handler.FileHandler;
import de.thedead2.progression_reloaded.util.logger.MissingAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownAdvancementFilter;
import de.thedead2.progression_reloaded.util.logger.UnknownRecipeCategoryFilter;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static de.thedead2.progression_reloaded.util.ModHelper.*;

@Mod(MOD_ID)
public class ProgressionReloaded {
    //TODO: extra class for event managing
    //TODO: Maybe better use Codecs for serialization and deserialization?
    //TODO: Level and quest builder classes --> + graphical ui of them
    //TODO: Use Gson and GsonHelper for whole project instead of json --> easier!
    //TODO: Instead of IForgeRegistry simple Maps for Levels and Quests? --> in game reload possible

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

        ModRegistries.register(new ProgressionQuest(
                new ResourceLocation(ModHelper.MOD_ID, "quest_test"),
                Component.literal("Test Quest"),
                Component.literal("This is a test quest!"),
                Items.ACACIA_BOAT.getDefaultInstance(),
                Set.of(new ItemReward(Items.DIAMOND.getDefaultInstance(), 50)),
                Map.of(
                        "testKill", new KillTrigger(PlayerPredicate.ANY,
                                new EntityPredicate(
                                        EntityTypePredicate.from(EntityType.CHICKEN),
                                        DistancePredicate.ANY,
                                        LocationPredicate.ANY,
                                        LocationPredicate.ANY,
                                        EffectsPredicate.ANY,
                                        NbtPredicate.ANY,
                                        new EntityFlagsPredicate(true, null, null, null, null),
                                        EntityEquipmentPredicate.ANY
                                )
                        ),
                        "testKill2", new KillTrigger(PlayerPredicate.ANY,
                                new EntityPredicate(
                                        EntityTypePredicate.from(EntityType.SPIDER),
                                        DistancePredicate.ANY,
                                        LocationPredicate.ANY,
                                        LocationPredicate.ANY,
                                        EffectsPredicate.ANY,
                                        NbtPredicate.ANY,
                                        EntityFlagsPredicate.ANY,
                                        EntityEquipmentPredicate.ANY
                                )
                        ),
                        "testSleep", new SleepTrigger(PlayerPredicate.ANY,
                                new LocationPredicate(
                                        MinMax.Doubles.ANY,
                                        MinMax.Doubles.ANY,
                                        MinMax.Doubles.ANY,
                                        Biomes.DESERT,
                                        null,
                                        null,
                                        BlockPredicate.ANY,
                                        FluidPredicate.ANY
                                )
                        )
                ),
                CriteriaStrategy.AND,
                RewardStrategy.ALL,
                true,
                null
        ));
        ModRegistries.register(new ProgressionQuest(
                new ResourceLocation(ModHelper.MOD_ID, "quest_test2"),
                Component.literal("Test Quest2"),
                Component.literal("This is a test quest2!"),
                Items.ACACIA_BOAT.getDefaultInstance(),
                Set.of(new SpawnEntityReward(EntityType.COMMAND_BLOCK_MINECART), new ItemReward(Items.ACACIA_BUTTON.getDefaultInstance(), 34)),
                Map.of("testKill2", new KillTrigger(PlayerPredicate.ANY, new EntityPredicate(
                        EntityTypePredicate.from(EntityType.HORSE),
                        DistancePredicate.ANY,
                        LocationPredicate.ANY,
                        LocationPredicate.ANY,
                        EffectsPredicate.ANY,
                        NbtPredicate.ANY,
                        EntityFlagsPredicate.ANY,
                        EntityEquipmentPredicate.ANY
                ))),
                CriteriaStrategy.OR,
                RewardStrategy.ALL,
                false,
                null
        ));
        ModRegistries.register(new ProgressionQuest(
                new ResourceLocation(ModHelper.MOD_ID, "quest_test3"),
                Component.literal("Test Quest3"),
                Component.literal("This is a test quest3!"),
                Items.ACACIA_BOAT.getDefaultInstance(),
                Set.of(new CommandReward("weather rain")),
                Map.of("test1", new KillTrigger(PlayerPredicate.ANY, new EntityPredicate(
                        EntityTypePredicate.from(EntityType.CREEPER),
                        DistancePredicate.ANY,
                        LocationPredicate.ANY,
                        LocationPredicate.ANY,
                        EffectsPredicate.ANY,
                        NbtPredicate.ANY,
                        EntityFlagsPredicate.ANY,
                        EntityEquipmentPredicate.ANY
                ))),
                CriteriaStrategy.AND,
                RewardStrategy.ALL,
                true,
                null
        ));
        ModRegistries.register(new ProgressionQuest(
                new ResourceLocation(ModHelper.MOD_ID, "quest_test4"),
                Component.literal("Test Quest4"),
                Component.literal("This is a test quest4!"),
                Items.ACACIA_BOAT.getDefaultInstance(),
                Set.of(new TeleportReward(new TeleportReward.TeleportDestination(5, 120, 120, 0, 0, ServerLevel.END))),
                Map.of("test2", new SleepTrigger(PlayerPredicate.ANY)),
                CriteriaStrategy.AND,
                RewardStrategy.ALL,
                true,
                new ResourceLocation(ModHelper.MOD_ID, "quest_test3")
        ));
        ModRegistries.register(new ProgressionLevel(
                -1,
                "creative-level",
                new ResourceLocation(ModHelper.MOD_ID, "creative_level"),
                RewardStrategy.ALL,
                Collections.emptySet(),
                Collections.emptySet(),
                null,
                null
        ));
        ModRegistries.register(new ProgressionLevel(
                0,
                "test",
                ResourceLocation.tryBuild(MOD_ID, "test-level"),
                RewardStrategy.ALL,
                Set.of(new ResourceLocation(ModHelper.MOD_ID, "quest_test"), new ResourceLocation(ModHelper.MOD_ID, "quest_test2")),
                Set.of(new ItemReward(Items.ITEM_FRAME.getDefaultInstance(), 5)),
                null,
                ResourceLocation.tryBuild(MOD_ID, "test-level2")
        ));
        ModRegistries.register(new ProgressionLevel(
                1,
                "test2",
                ResourceLocation.tryBuild(MOD_ID, "test-level2"),
                RewardStrategy.ALL,
                Set.of(new ResourceLocation(ModHelper.MOD_ID, "quest_test3"), new ResourceLocation(ModHelper.MOD_ID, "quest_test4")),
                Set.of(new ItemReward(Items.HORSE_SPAWN_EGG.getDefaultInstance(), 1)),
                ResourceLocation.tryBuild(MOD_ID, "test-level"),
                null
        ));

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
        forgeEventBus.addListener(this::onPlayerLoggedOut);
        forgeEventBus.addListener(this::onGameShuttingDown);
        forgeEventBus.addListener(this::onGameTick);

        forgeEventBus.register(this);
        registerLoggerFilter();
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
