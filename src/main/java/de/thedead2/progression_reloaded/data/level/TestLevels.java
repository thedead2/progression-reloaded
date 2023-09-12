package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Set;


public class TestLevels {
    public static final ProgressionLevel CREATIVE = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("creative_level")
                                    .withName("creative-level")
                                    .build(),
            Rewards.Builder.builder().build(),
            Collections.emptySet()
    );

    public static final ProgressionLevel TEST1 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level")
                                    .withName("test")
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
                                    .withName("test2")
                                    .withParent("test-level")
                                    .build(),
            Rewards.Builder.builder()
                           .withReward(new ItemReward(Items.HORSE_SPAWN_EGG.getDefaultInstance(), 1))
                           .build(),
            Set.of(
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test3"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test4"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test5")
            )
    );

    public static final ProgressionLevel TEST3 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level3")
                                    .withName("test3")
                                    .withParent("test-level2")
                                    .build(),
            Rewards.Builder.builder().build(),
            Collections.emptySet()
    );

    public static final ProgressionLevel TEST4 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level4")
                                    .withName("test4")
                                    .build(),
            Rewards.Builder.builder().build(),
            Collections.emptySet()
    );

    public static final ProgressionLevel TEST5 = new ProgressionLevel(
            LevelDisplayInfo.Builder.builder()
                                    .withId("test-level5")
                                    .withName("test5")
                                    .build(),
            Rewards.Builder.builder().build(),
            Collections.emptySet()
    );
}
