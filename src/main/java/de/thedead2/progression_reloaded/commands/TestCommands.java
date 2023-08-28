package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.CrashReport;


public class TestCommands {

    public static void register() {
        if(!ModHelper.isDevEnv()) {
            return;
        }

        ModCommand.Builder.newModCommand("test/crash", context -> {
            CrashHandler.getInstance().printCrashReport(new CrashReport("Test crash", new Throwable()));
            return ModCommand.COMMAND_SUCCESS;
        });
    }
}
