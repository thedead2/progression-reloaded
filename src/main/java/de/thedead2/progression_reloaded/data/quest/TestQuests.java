package de.thedead2.progression_reloaded.data.quest;

import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.predicates.*;
import de.thedead2.progression_reloaded.data.rewards.ExtraLifeReward;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.data.tasks.QuestTasks;
import de.thedead2.progression_reloaded.data.tasks.TaskStrategy;
import de.thedead2.progression_reloaded.data.trigger.BreakBlockCriterionTrigger;
import de.thedead2.progression_reloaded.data.trigger.ItemPickupCriterionTrigger;
import de.thedead2.progression_reloaded.data.trigger.KillCriterionTrigger;
import de.thedead2.progression_reloaded.data.trigger.QuestCompleteCriterionTrigger;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static de.thedead2.progression_reloaded.util.ModHelper.isDevEnv;


public class TestQuests {

    public static final ProgressionQuest TEST1 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("test_quest_1")
                                    .withName("Test Quest 1")
                                    .withDescription("This is a test quest!")
                                    .withIcon(Items.ANVIL)
                                    .isMainQuest()
                                    .build(),
            QuestTasks.Builder.builder()
                              .withStartTask("kill_a_zombie", new KillCriterionTrigger(EntityPredicate.from(EntityType.ZOMBIE)), Rewards.empty(), Component.empty(), TaskStrategy.AND, "kill_a_horse", "kill_a_cow", "kill_a_wolf")
                              .withTask("kill_a_horse", new KillCriterionTrigger(EntityPredicate.from(EntityType.HORSE), MinMax.Ints.ANY, MinMax.Doubles.exactly(MathHelper.secondsToTicks(20f))), Rewards.empty(), Component.literal("Kill a horse!"), false, false, TaskStrategy.OR, "kill_a_spider")
                              .withTask("kill_a_cow", new KillCriterionTrigger(EntityPredicate.from(EntityType.COW), MinMax.Ints.atLeast(5), MinMax.Doubles.ANY), Rewards.empty(), Component.literal("Kill a cow!"), false, false, TaskStrategy.OR, "find_a_diamond")
                              .withTask("kill_a_wolf", new KillCriterionTrigger(EntityPredicate.from(EntityType.WOLF), MinMax.Ints.ANY, MinMax.Doubles.exactly(MathHelper.secondsToTicks(30f))), Rewards.Builder.builder()
                                                                                                                                                                                                                .withReward(new ItemReward(Items.AMETHYST_BLOCK, 5))
                                                                                                                                                                                                                .build(), Component.empty(), true, false, TaskStrategy.OR)
                              .withTask("kill_a_spider", new KillCriterionTrigger(EntityPredicate.from(EntityType.SPIDER)), Rewards.empty(), Component.literal("Kill a spider!"), false, true, TaskStrategy.OR, "find_a_stick")
                              .withEndTask("find_a_diamond", new ItemPickupCriterionTrigger(ItemPredicate.from(Items.DIAMOND)),
                                           Rewards.Builder.builder().withReward(new ItemReward(Items.DIAMOND.getDefaultInstance(), 50)).build(),
                                           Component.literal("Find a diamond!"),
                                           true
                                )
                              .withEndTask("find_a_stick", new ItemPickupCriterionTrigger(ItemPredicate.from(Items.STICK)),
                                           Rewards.Builder.builder().withReward(new ItemReward(Items.STICK.getDefaultInstance(), 50)).build(),
                                           Component.literal("Find a stick!"),
                                           false
                              )
                              .build()
    );

    public static final ProgressionQuest TEST2 = new ProgressionQuest(
            QuestDisplayInfo.Builder.builder()
                                    .withId("test_quest_2")
                                    .withName("Test Quest 2")
                                    .withDescription("This is a test quest too!")
                                    .withIcon(Items.DIAMOND)
                                    .build(),
            QuestTasks.Builder.builder()
                              .withStartTask("start", new QuestCompleteCriterionTrigger(QuestPredicate.from(new ResourceLocation(ModHelper.MOD_ID, "test_quest_1"))), Rewards.Builder.builder().withReward(new ItemReward(Items.STICK, 1)).build(), Component.empty(), TaskStrategy.OR, "find_an_anvil")
                              .withTask("find_an_anvil", new ItemPickupCriterionTrigger(ItemPredicate.from(Items.ANVIL)), Rewards.empty(), Component.literal("Find an anvil!"), false, false, TaskStrategy.OR, "break_the_anvil")
                              .withTask("break_the_anvil", new BreakBlockCriterionTrigger(BlockPredicate.from(Blocks.ANVIL)), Rewards.empty(), Component.literal("Break the anvil!"), false, false, TaskStrategy.OR, "kill_a_horse")
                              .withEndTask("kill_a_horse", new KillCriterionTrigger(EntityPredicate.from(EntityType.HORSE)),
                                           Rewards.Builder.builder().withReward(new ExtraLifeReward()).build(),
                                           Component.literal("Kill a horse!"),
                                         true
                                )
                                .build()

    );


    public static void register() {
        if(isDevEnv()) {
            ModRegistries.register(TestQuests.TEST1);
            ModRegistries.register(TestQuests.TEST2);
        }
    }
}
