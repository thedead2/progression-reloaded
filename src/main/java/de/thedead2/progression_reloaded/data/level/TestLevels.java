package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Set;


public class TestLevels {
    public static final ProgressionLevel TEST1 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level")
                                    .withName("Test Level 1")
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new ItemReward(Items.ITEM_FRAME.getDefaultInstance(), 5))
                           .build(),
            Set.of(
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test2")
            )
    );

    public static final ProgressionLevel TEST2 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level2")
                                    .withName("Test Level 2")
                                    .withParent("test-level")
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new ItemReward(Items.HORSE_SPAWN_EGG.getDefaultInstance(), 1))
                           .build(),
            Set.of(
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test3"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test4"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test5"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test6"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test7"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test8")

            )
    );
}
