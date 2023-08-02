package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.network.chat.Component;

public class DevCommands {
    public static void register(){
        if(!ModHelper.isDevEnv()) return;

        ModCommand.Builder.newModCommand("dev/level/questManager", context -> {
            SinglePlayer player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
            var level = player.getProgressionLevel();
            context.getSource().sendSuccess(Component.literal("Current questManager for level " + level.getName() + ": " + level.getQuestManager().toString()), true);
            return ModCommand.COMMAND_SUCCESS;
        });
    }
}
