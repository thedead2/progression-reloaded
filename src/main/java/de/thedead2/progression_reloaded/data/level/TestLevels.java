package de.thedead2.progression_reloaded.data.level;

import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.rewards.ItemReward;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Set;

import static de.thedead2.progression_reloaded.util.ModHelper.isDevEnv;


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
                    new ResourceLocation(ModHelper.MOD_ID, "test_quest_1"),
                    new ResourceLocation(ModHelper.MOD_ID, "test_quest_2")
            )
    );


    public static void register() {
        if(isDevEnv()) {
            ModRegistries.register(TestLevels.TEST1);
        }
    }
}
