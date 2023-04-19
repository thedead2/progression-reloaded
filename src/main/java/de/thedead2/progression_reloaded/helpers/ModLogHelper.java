package de.thedead2.progression_reloaded.helpers;

import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;

public class ModLogHelper {
    public static void log(String mod, String text) {
        if (Loader.isModLoaded(mod)) {
            de.thedead2.progression_reloaded.ProgressionReloaded.LOGGER.log(Level.INFO, text);
        }
    }
}
