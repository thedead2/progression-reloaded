package de.thedead2.progression_reloaded.util.registries;

import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public abstract class RegistryKeys {
    public static final ResourceKey<Registry<ProgressionLevel>> LEVELS = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "levels"));
    public static final ResourceKey<Registry<ProgressionQuest>> QUESTS = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "quests"));
    public static final ResourceKey<Registry<Class<SimpleTrigger<?>>>> TRIGGER = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "trigger"));
    public static final ResourceKey<Registry<Class<IReward>>> REWARDS = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "rewards"));
    public static final ResourceKey<Registry<Class<ITriggerPredicate<?>>>> PREDICATES = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "predicates"));
    public static final ResourceKey<Registry<Class<IAbility<?>>>> ABILITIES = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "abilities"));
}
