package de.thedead2.progression_reloaded.util.registries;

import de.thedead2.progression_reloaded.data.abilities.IAbility;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.rewards.IReward;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public abstract class DynamicRegistries {

    public static final Map<ResourceLocation, Class<SimpleTrigger<?>>> PROGRESSION_TRIGGER = new HashMap<>();

    public static final Map<ResourceLocation, Class<IReward>> PROGRESSION_REWARDS = new HashMap<>();

    public static final Map<ResourceLocation, Class<ITriggerPredicate<?>>> PROGRESSION_PREDICATES = new HashMap<>();

    public static final Map<ResourceLocation, Class<IAbility<?>>> PROGRESSION_ABILITIES = new HashMap<>();


    @SuppressWarnings("unchecked")
    public static <T, V extends T> void registerClasses(Class<T> baseClass, Map<ResourceLocation, Class<V>> map) {
        ReflectionHelper.findMatchingClasses(baseClass).forEach(aClass -> {
            ResourceLocation id;
            try {
                id = new ResourceLocation(aClass.getDeclaredField("ID").get(null).toString());
            }
            catch(IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            register(id, (Class<T>) aClass, map, baseClass);
        });
    }


    @SuppressWarnings("unchecked")
    public static <T, V extends T> void register(ResourceLocation id, Class<T> clazz, Map<ResourceLocation, Class<V>> map, Class<T> baseClass) {
        map.putIfAbsent(id, (Class<V>) ReflectionHelper.changeClassLoader(clazz, baseClass.getClassLoader()));
    }
}
