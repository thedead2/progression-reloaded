package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.RewardStrategy;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Set;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class TestLevels {

    public static final ProgressionLevel CREATIVE = new ProgressionLevel(
            "creative-level",
            new ResourceLocation(ModHelper.MOD_ID, "creative_level"),
            RewardStrategy.ALL,
            Collections.emptySet(),
            Collections.emptySet(),
            null,
            null
    );

    public static final ProgressionLevel TEST1 = new ProgressionLevel(
            "test",
            ResourceLocation.tryBuild(MOD_ID, "test-level"),
            RewardStrategy.ALL,
            Set.of(
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test2")
            ),
            Set.of(new ItemReward(Items.ITEM_FRAME.getDefaultInstance(), 5)),
            null,
            ResourceLocation.tryBuild(MOD_ID, "test-level2")
    );

    public static final ProgressionLevel TEST2 = new ProgressionLevel(
            "test2",
            ResourceLocation.tryBuild(MOD_ID, "test-level2"),
            RewardStrategy.ALL,
            Set.of(
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test3"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test4"),
                    new ResourceLocation(ModHelper.MOD_ID, "quest_test5")
            ),
            Set.of(new ItemReward(Items.HORSE_SPAWN_EGG.getDefaultInstance(), 1)),
            ResourceLocation.tryBuild(MOD_ID, "test-level"),
            null
    );
}
