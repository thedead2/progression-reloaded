package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.chat.Component;


public class DevCommands {

    public static void register() {
        if(!ModHelper.isDevEnv()) {
            return;
        }

        ModCommand.Builder.newModCommand("dev/level/questManager", context -> {
            PlayerData player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
            var level = player.getProgressionLevel();
            context.getSource().sendSuccess(Component.literal("Current questManager for level " + level.getTitle() + ": " + level.getQuests().toString()), true);
            return ModCommand.COMMAND_SUCCESS;
        });

        ModCommand.Builder.newModCommand("dev/lives/add", context -> {
            if(ExtraLifeItem.rewardExtraLife(context.getSource().getPlayerOrException(), true)) {
                return ModCommand.COMMAND_SUCCESS;
            }
            else {
                return ModCommand.COMMAND_FAILURE;
            }
        });

        ModCommand.Builder.newModCommand("dev/lives/unlimited", context -> {
            var src = context.getSource();
            if(ExtraLifeItem.unlimited()) {
                src.sendSuccess(Component.literal("Enabled unlimited lives!"), false);
            }
            else {
                src.sendSuccess(Component.literal("Disabled unlimited lives!"), false);
            }
            return ModCommand.COMMAND_SUCCESS;
        });
    }
}
