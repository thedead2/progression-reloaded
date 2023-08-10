package de.thedead2.progression_reloaded.data.quest;

import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.predicates.*;
import de.thedead2.progression_reloaded.data.rewards.*;
import de.thedead2.progression_reloaded.data.trigger.KillTrigger;
import de.thedead2.progression_reloaded.data.trigger.PlacedBlockTrigger;
import de.thedead2.progression_reloaded.data.trigger.SleepTrigger;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

public class TestQuests {
    public static final ProgressionQuest TEST1 = new ProgressionQuest(
            new ResourceLocation(ModHelper.MOD_ID, "quest_test"),
            Component.literal("Test Quest"),
            Component.literal("This is a test quest!"),
            Items.ACACIA_BOAT.getDefaultInstance(),
            Set.of(new ItemReward(Items.DIAMOND.getDefaultInstance(), 50)),
            Map.of(
                    "testKill", new KillTrigger(PlayerPredicate.ANY,
                            new EntityPredicate(
                                    EntityTypePredicate.from(EntityType.COW),
                                    DistancePredicate.ANY,
                                    LocationPredicate.ANY,
                                    LocationPredicate.ANY,
                                    EffectsPredicate.ANY,
                                    NbtPredicate.ANY,
                                    new EntityFlagsPredicate(false, null, null, null, true),
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
    );
    public static final ProgressionQuest TEST2 = new ProgressionQuest(
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
    );
    public static final ProgressionQuest TEST3 = new ProgressionQuest(
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
    );
    public static final ProgressionQuest TEST4 = new ProgressionQuest(
            new ResourceLocation(ModHelper.MOD_ID, "quest_test4"),
            Component.literal("Test Quest4"),
            Component.literal("This is a test quest4!"),
            Items.ACACIA_BOAT.getDefaultInstance(),
            Set.of(new TeleportReward(new TeleportReward.TeleportDestination(5, 120, 120, 0, 0, ServerLevel.END))),
            Map.of("test2", new PlacedBlockTrigger(PlayerPredicate.ANY,
                    new BlockPredicate(
                            null,
                            Blocks.EMERALD_BLOCK,
                            (StatePropertiesPredicate<BlockState>) StatePropertiesPredicate.ANY,
                            NbtPredicate.ANY
                    )
            )),
            CriteriaStrategy.AND,
            RewardStrategy.ALL,
            true,
            new ResourceLocation(ModHelper.MOD_ID, "quest_test3")
    );
}
