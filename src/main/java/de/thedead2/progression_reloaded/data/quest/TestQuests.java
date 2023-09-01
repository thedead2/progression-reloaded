package de.thedead2.progression_reloaded.data.quest;

import de.thedead2.progression_reloaded.client.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.predicates.*;
import de.thedead2.progression_reloaded.data.rewards.*;
import de.thedead2.progression_reloaded.data.trigger.KillTrigger;
import de.thedead2.progression_reloaded.data.trigger.PlacedBlockTrigger;
import de.thedead2.progression_reloaded.data.trigger.PlayerInventoryChangedTrigger;
import de.thedead2.progression_reloaded.data.trigger.SleepTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;


public class TestQuests {

    public static final ProgressionQuest TEST1 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test")
                                    .withName("Test Quest")
                                    .withDescription("This is a test quest!")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new ItemReward(Items.DIAMOND.getDefaultInstance(), 50))
                           .build(),
            QuestCriteria.Builder.builder()
                                 .withCriterion(
                                         "testKill",
                                         new KillTrigger(
                                                 PlayerPredicate.ANY,
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
                                         )
                                 )
                                 .withCriterion(
                                         "testKill2",
                                         new KillTrigger(
                                                 PlayerPredicate.ANY,
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
                                         )
                                 )
                                 .withCriterion(
                                         "testSleep",
                                         new SleepTrigger(
                                                 PlayerPredicate.ANY,
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
                                 )
                                 .build()
    );

    public static final ProgressionQuest TEST2 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test2")
                                    .withName("Test Quest 2")
                                    .withDescription("This is a test quest2!")
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new SpawnEntityReward(EntityType.COMMAND_BLOCK_MINECART))
                           .withReward(new ItemReward(Items.ACACIA_BUTTON.getDefaultInstance(), 34))
                           .withReward(new ExtraLifeReward())
                           .build(),
            QuestCriteria.Builder.builder()
                                 .withCriterion(
                                         "testKill2",
                                         new KillTrigger(
                                                 PlayerPredicate.ANY,
                                                 new EntityPredicate(
                                                         EntityTypePredicate.from(EntityType.HORSE),
                                                         DistancePredicate.ANY,
                                                         LocationPredicate.ANY,
                                                         LocationPredicate.ANY,
                                                         EffectsPredicate.ANY,
                                                         NbtPredicate.ANY,
                                                         EntityFlagsPredicate.ANY,
                                                         EntityEquipmentPredicate.ANY
                                                 )
                                         )
                                 )
                                 .withStrategy(CriteriaStrategy.OR)
                                 .build()
    );

    public static final ProgressionQuest TEST3 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test3")
                                    .withName("Test Quest3")
                                    .withDescription("This is a test quest3!")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new CommandReward("weather rain"))
                           .build(),
            QuestCriteria.Builder.builder()
                                 .withCriterion(
                                         "test1",
                                         new KillTrigger(
                                                 PlayerPredicate.ANY,
                                                 new EntityPredicate(
                                                         EntityTypePredicate.from(EntityType.CREEPER),
                                                         DistancePredicate.ANY,
                                                         LocationPredicate.ANY,
                                                         LocationPredicate.ANY,
                                                         EffectsPredicate.ANY,
                                                         NbtPredicate.ANY,
                                                         EntityFlagsPredicate.ANY,
                                                         EntityEquipmentPredicate.ANY
                                                 )
                                         )
                                 )
                                 .build()
    );

    @SuppressWarnings("unchecked")
    public static final ProgressionQuest TEST4 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test4")
                                    .withName("Test Quest4")
                                    .withDescription("This is a test quest4!")
                                    .withParent("quest_test3")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new TeleportReward(new TeleportReward.TeleportDestination(5, 120, 120, 0, 0, ServerLevel.END)))
                           .build(),
            QuestCriteria.Builder.builder()
                                 .withCriterion(
                                         "test2",
                                         new PlacedBlockTrigger(
                                                 PlayerPredicate.ANY,
                                                 new BlockPredicate(
                                                         null,
                                                         Blocks.EMERALD_BLOCK,
                                                         (StatePropertiesPredicate<BlockState>) StatePropertiesPredicate.ANY,
                                                         NbtPredicate.ANY
                                                 )
                                         )
                                 )
                                 .build()

    );

    public static final ProgressionQuest TEST5 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test5")
                                    .withName("Test Quest5")
                                    .withDescription("This is a test quest5!")
                                    .withParent("quest_test4")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new EffectReward(new MobEffectInstance(MobEffects.LEVITATION, 50)))
                           .build(),
            QuestCriteria.Builder.builder()
                                 .withCriterion(
                                         "test34",
                                         new PlayerInventoryChangedTrigger(
                                                 PlayerPredicate.ANY,
                                                 new ItemPredicate(
                                                         Items.ANVIL,
                                                         MinMax.Ints.ANY,
                                                         MinMax.Ints.ANY,
                                                         Collections.emptySet(),
                                                         Collections.emptySet(),
                                                         NbtPredicate.ANY,
                                                         null
                                                 )
                                         )
                                 )
                                 .build()

    );
}
