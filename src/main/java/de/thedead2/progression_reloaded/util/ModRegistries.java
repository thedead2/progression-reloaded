package de.thedead2.progression_reloaded.util;

import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ModRegistries {

    private static final DeferredRegister<ProgressionLevel> PROGRESSION_LEVEL_DEFERRED_REGISTER = DeferredRegister.create(Keys.LEVEL, ModHelper.MOD_ID);
    public static final Supplier<IForgeRegistry<ProgressionLevel>> PROGRESSION_LEVEL = PROGRESSION_LEVEL_DEFERRED_REGISTER.makeRegistry(RegistryBuilder::new);

    public static void register(IEventBus bus) {
        PROGRESSION_LEVEL_DEFERRED_REGISTER.register(bus);
        //TEAM_DATA & PLAYER_DATA individual per world --> DONE!
        //Trigger and criteria per Registry
        //ProgressionLevel per Registry
    }

    public static class Keys{
        public static final ResourceKey<Registry<ProgressionLevel>> LEVEL = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "level"));
    }
}
