package de.thedead2.progression_reloaded.util.plugin;

import de.thedead2.progression_reloaded.api.plugin.IProgressionPlugin;
import de.thedead2.progression_reloaded.api.plugin.ProgressionPlugin;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.*;


public final class PluginFinder {

    private static final Logger LOGGER = LogManager.getLogger();


    private PluginFinder() {

    }


    public static List<IProgressionPlugin> getModPlugins() {
        return getInstances(ProgressionPlugin.class, IProgressionPlugin.class);
    }


    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
        Type annotationType = Type.getType(annotationClass);
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        Set<String> pluginClassNames = new LinkedHashSet<>();
        for(ModFileScanData scanData : allScanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for(ModFileScanData.AnnotationData a : annotations) {
                if(Objects.equals(a.annotationType(), annotationType)) {
                    String memberName = a.memberName();
                    pluginClassNames.add(memberName);
                }
            }
        }
        List<T> instances = new ArrayList<>();
        for(String className : pluginClassNames) {
            try {
                Class<?> asmClass = Class.forName(className);
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
                T instance = constructor.newInstance();
                instances.add(instance);
            }
            catch(ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load: {}", className, e);
            }
        }
        return instances;
    }
}