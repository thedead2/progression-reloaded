package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Sets;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.predicates.*;
import de.thedead2.progression_reloaded.data.rewards.ExtraLifeReward;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.trigger.BreakBlockTrigger;
import de.thedead2.progression_reloaded.data.trigger.ItemPickupTrigger;
import de.thedead2.progression_reloaded.data.trigger.KillTrigger;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static de.thedead2.progression_reloaded.util.ModHelper.isDevEnv;


public class TestQuests {

    public static final ProgressionQuest TEST1 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test")
                                    .withName("Test Quest")
                                    .withDescription("This is a test quest!")
                                    .withIcon(Items.ANVIL)
                                    .isMainQuest()
                                    .build(),
            QuestTasks.Builder.builder()
                              .withStartTask("test1_start", QuestCriteria.Builder.builder().withCriterion("killACreeper", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.CREEPER))).build(), Rewards.empty(), Component.empty(), "test1_a", "test1_b")
                              .withTask("test1_a", QuestCriteria.Builder.builder().withCriterion("killAHorse", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.HORSE))).build(), Rewards.empty(), Component.literal("Kill a horse!"), false, false, "test1_c")
                              .withTask("test1_b", QuestCriteria.Builder.builder().withCriterion("killACow", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.COW))).build(), Rewards.empty(), Component.literal("Kill a cow!"), false, false, "test1_end")
                              .withTask("test1_c", QuestCriteria.Builder.builder().withCriterion("killASpider", new KillTrigger(PlayerPredicate.ANY, EntityPredicate.from(EntityType.SPIDER))).build(), Rewards.empty(), Component.literal("Kill a spider!"), false, true, "test1_end_failed")
                              .withEndTask("test1_end", QuestCriteria.Builder.builder().withCriterion("findDiamond", new ItemPickupTrigger(PlayerPredicate.ANY, new ItemPredicate(Items.DIAMOND, MinMax.Ints.ANY, MinMax.Ints.ANY, Sets.newHashSet(), Sets.newHashSet(), NbtPredicate.ANY, null))).build(),
                                           Rewards.Builder.builder().withReward(new ItemReward(Items.DIAMOND.getDefaultInstance(), 50)).build(),
                                           Component.literal("Find a diamond!"),
                                           true
                                )
                              .withEndTask("test1_end_failed", QuestCriteria.Builder.builder()
                                                                                    .withCriterion("findStick", new ItemPickupTrigger(PlayerPredicate.ANY, new ItemPredicate(Items.STICK, MinMax.Ints.ANY, MinMax.Ints.ANY, Sets.newHashSet(), Sets.newHashSet(), NbtPredicate.ANY, null)))
                                                                                    .build(),
                                           Rewards.Builder.builder().withReward(new ItemReward(Items.STICK.getDefaultInstance(), 50)).build(),
                                           Component.literal("Find a stick!"),
                                           false
                              )
                              .build()
    );

    public static final ProgressionQuest TEST2 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test2")
                                    .withName("Test Quest 2")
                                    .withDescription("This is a test quest2!")
                                    .build(),
            QuestTasks.Builder.builder()
                              .withStartTask("test2_start", QuestCriteria.requiresParentComplete("quest_test", null), Rewards.Builder.builder().withReward(new ItemReward(Items.STICK, 1)).build(), Component.empty(), "test2_a")
                              .withTask("test2_a", QuestCriteria.Builder.builder()
                                                                        .withCriterion("findAnvil", new ItemPickupTrigger(PlayerPredicate.ANY, new ItemPredicate(Items.ANVIL, MinMax.Ints.ANY, MinMax.Ints.ANY, Sets.newHashSet(), Sets.newHashSet(), NbtPredicate.ANY, null)))
                                                                        .build(), Rewards.empty(), Component.literal("Find an anvil!"), false, false, "test2_b")
                              .withTask("test2_b", QuestCriteria.Builder.builder()
                                                                        .withCriterion("breakTheAnvil", new BreakBlockTrigger(PlayerPredicate.ANY, new BlockPredicate(null, Blocks.ANVIL, (StatePropertiesPredicate<BlockState>) StatePropertiesPredicate.ANY, NbtPredicate.ANY)))
                                                                        .build(), Rewards.empty(), Component.literal("Break the anvil!"), false, false, "test2_end")
                              .withEndTask("test2_end", QuestCriteria.Builder.builder()
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
                                                                           .build(),
                                           Rewards.Builder.builder().withReward(new ExtraLifeReward()).build(),
                                           Component.literal("Kill a horse!"),
                                         true
                                )
                                .build()

    );

    /*

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
            QuestActions.Builder.builder()
                                .withStart("test3_start", QuestCriteria.requiresParentComplete("quest_test2"), Component.empty(), "test3_end")
                                .withEnd("test3_end", QuestCriteria.Builder.builder()
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
                                                                           .build(),
                                         Component.empty(),
                                         true
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
            QuestActions.Builder.builder()
                                .withStart("test4_start", QuestCriteria.requiresParentComplete("quest_test3"), Component.empty(), "test4_end")
                                .withEnd("test4_end", QuestCriteria.Builder.builder()
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
                                                                           .build(),
                                         Component.empty(),
                                         true
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
                           .withReward(new EffectReward(new MobEffectInstance(MobEffects.LEVITATION, MathHelper.secondsToTicks(10))))
                           .build(),
            QuestActions.Builder.builder()
                                .withStart("test5_start", QuestCriteria.requiresParentComplete("quest_test4"), Component.empty(), "test5_end")
                                .withEnd("test5_end", QuestCriteria.Builder.builder()
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
                                                                           .build(),
                                         Component.empty(),
                                         true
                                )
                                .build()


    );

    public static final ProgressionQuest TEST6 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test6")
                                    .withName("Test Quest6")
                                    .withDescription("This is a test quest6!")
                                    .withParent("quest_test5")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new EffectReward(new MobEffectInstance(MobEffects.HEAL, MathHelper.secondsToTicks(10))))
                           .build(),
            QuestActions.Builder.builder()
                                .withStart("test6_start", QuestCriteria.requiresParentComplete("quest_test5"), Component.empty(), "test6_end")
                                .withEnd("test6_end", QuestCriteria.Builder.builder()
                                                                           .withCriterion(
                                                                                   "test35",
                                                                                   new PlayerInventoryChangedTrigger(
                                                                                           PlayerPredicate.ANY,
                                                                                           new ItemPredicate(
                                                                                                   Items.ACACIA_BUTTON,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   Collections.emptySet(),
                                                                                                   Collections.emptySet(),
                                                                                                   NbtPredicate.ANY,
                                                                                                   null
                                                                                           )
                                                                                   )
                                                                           )
                                                                           .build(),
                                         Component.empty(),
                                         true
                                )
                                .build()


    );

    public static final ProgressionQuest TEST7 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test7")
                                    .withName("Test Quest7")
                                    .withDescription("This is a test quest7!")
                                    .withParent("quest_test6")
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new EffectReward(new MobEffectInstance(MobEffects.WATER_BREATHING, MathHelper.secondsToTicks(10))))
                           .build(),
            QuestActions.Builder.builder()
                                .withStart("test7_start", QuestCriteria.requiresParentComplete("quest_test6"), Component.empty(), "test7_end")
                                .withEnd("test7_end", QuestCriteria.Builder.builder()
                                                                           .withCriterion(
                                                                                   "test36",
                                                                                   new PlayerInventoryChangedTrigger(
                                                                                           PlayerPredicate.ANY,
                                                                                           new ItemPredicate(
                                                                                                   Items.STONE,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   Collections.emptySet(),
                                                                                                   Collections.emptySet(),
                                                                                                   NbtPredicate.ANY,
                                                                                                   null
                                                                                           )
                                                                                   )
                                                                           )
                                                                           .build(),
                                         Component.empty(),
                                         true
                                )
                                .build()


    );

    public static final ProgressionQuest TEST8 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("quest_test8")
                                    .withName("Test Quest8")
                                    .withDescription("This is a test quest8!")
                                    .withParent("quest_test7")
                                    .withIcon(Items.DIAMOND)
                                    .isMainQuest()
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new EffectReward(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, MathHelper.secondsToTicks(10))))
                           .build(),
            QuestActions.Builder.builder()
                                .withStart("test8_start", QuestCriteria.requiresParentComplete("quest_test7"), Component.empty(), "test8_end")
                                .withEnd("test8_end", QuestCriteria.Builder.builder()
                                                                           .withCriterion(
                                                                                   "test37",
                                                                                   new PlayerInventoryChangedTrigger(
                                                                                           PlayerPredicate.ANY,
                                                                                           new ItemPredicate(
                                                                                                   Items.PACKED_ICE,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   MinMax.Ints.ANY,
                                                                                                   Collections.emptySet(),
                                                                                                   Collections.emptySet(),
                                                                                                   NbtPredicate.ANY,
                                                                                                   null
                                                                                           )
                                                                                   )
                                                                           )
                                                                           .build(),
                                         Component.empty(),
                                         true
                                )
                                .build()


    );*/


    public static void register() {
        if(isDevEnv()) {
            ModRegistries.register(TestQuests.TEST1);
            ModRegistries.register(TestQuests.TEST2);
            //ModRegistries.register(TestQuests.TEST3);
            //ModRegistries.register(TestQuests.TEST4);
            //ModRegistries.register(TestQuests.TEST5);
            //ModRegistries.register(TestQuests.TEST6);
            //ModRegistries.register(TestQuests.TEST7);
            //ModRegistries.register(TestQuests.TEST8);
        }
    }
}
