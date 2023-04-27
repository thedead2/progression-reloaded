package de.thedead2.progression_reloaded.util;

import de.thedead2.progression_reloaded.player.PlayerData;
import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.TeamData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ModRegistries {

    private static final DeferredRegister<PlayerData> PROGRESSION_PLAYER_DATA_DEFERRED_REGISTER = DeferredRegister.create(Keys.PLAYER_DATA, ModHelper.MOD_ID);
    public static final Supplier<IForgeRegistry<PlayerData>> PROGRESSION_PLAYER_DATA = PROGRESSION_PLAYER_DATA_DEFERRED_REGISTER.makeRegistry(RegistryBuilder::new);
    private static final DeferredRegister<PlayerTeam> PROGRESSION_TEAM_DATA_DEFERRED_REGISTER = DeferredRegister.create(Keys.TEAM_DATA, ModHelper.MOD_ID);
    public static final Supplier<IForgeRegistry<PlayerTeam>> PROGRESSION_TEAM_DATA = PROGRESSION_TEAM_DATA_DEFERRED_REGISTER.makeRegistry(RegistryBuilder::new);

    public static void register(IEventBus bus) {
        PROGRESSION_TEAM_DATA_DEFERRED_REGISTER.register(bus);
        PROGRESSION_PLAYER_DATA_DEFERRED_REGISTER.register(bus);
    }

    public static class Keys{
        public static final ResourceKey<Registry<PlayerData>> PLAYER_DATA  = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "player_data"));
        public static final ResourceKey<Registry<PlayerTeam>> TEAM_DATA  = ResourceKey.createRegistryKey(new ResourceLocation(ModHelper.MOD_ID, "team_data"));
    }
}
