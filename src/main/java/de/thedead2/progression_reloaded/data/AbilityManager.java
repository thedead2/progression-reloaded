package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.abilities.managers.RestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.restrictions.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;


public abstract class AbilityManager { //TODO: implement abilities

    private static final Map<ResourceLocation, RestrictionManager<?, ?>> restrictionManagers = new HashMap<>();

    private static boolean registered = false;


    public static <T extends RestrictionManager<R, V>, R extends Restriction<V>, V> T registerManager(ResourceLocation managerId, T manager) {
        if(registered) {
            throw new IllegalStateException("RestrictionManagers have already been registered to event bus!"
                                                    + "\nTry registering your RestrictionManager before AddReloadListenerEvent is fired!");
        }
        else if(restrictionManagers.containsKey(managerId)) {
            throw new IllegalArgumentException("Duplicate manager registration with id: " + managerId);
        }
        else {
            restrictionManagers.put(managerId, manager);
            MinecraftForge.EVENT_BUS.register(manager);
            return manager;
        }
    }


    public static <T extends RestrictionManager<R, V>, R extends Restriction<V>, V> T getManagerForId(ResourceLocation managerId) {
        return (T) restrictionManagers.get(managerId);
    }


    @SubscribeEvent
    public static void registerReloadManagers(final AddReloadListenerEvent event) {
        restrictionManagers.values().forEach(event::addListener);
        registered = true;
    }


    public static void syncRestrictionsWithClient(ServerPlayer serverPlayer, boolean shouldReset) {
        restrictionManagers.forEach((resourceLocation, restrictionManager) -> restrictionManager.syncRestrictionsWithClient(serverPlayer, shouldReset));
    }
}
