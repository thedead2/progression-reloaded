package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.CrashReport;
import net.minecraft.network.chat.Component;


public class DevCommands {

    public static void register() {
        if(!ModHelper.isDevEnv()) {
            return;
        }

        ModCommand.Builder.builder()
                .withPath("dev/level/questManager")
                .withAction(context -> {
                    PlayerData player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
                    var level = player.getProgressionLevel();
                    context.getSource().sendSuccess(Component.literal("Current questManager for level " + level.getTitle() + ": " + level.getQuests().toString()), true);
                    return ModCommands.COMMAND_SUCCESS;
                })
                .buildAndRegister();

        ModCommand.Builder.builder().withPath("dev/lives/add")
                          .withAction(context -> {
                              if(ExtraLifeItem.rewardExtraLife(context.getSource().getPlayerOrException(), true)) {
                                  return ModCommands.COMMAND_SUCCESS;
                              }
                              else {
                                  return ModCommands.COMMAND_FAILURE;
                              }
                          })
                .buildAndRegister();

        ModCommand.Builder.builder()
                .withPath("dev/lives/unlimited")
                .withAction(context -> {
                    var src = context.getSource();
                    if(ExtraLifeItem.unlimited()) {
                        src.sendSuccess(Component.literal("Enabled unlimited lives!"), false);
                    }
                    else {
                        src.sendSuccess(Component.literal("Disabled unlimited lives!"), false);
                    }
                    return ModCommands.COMMAND_SUCCESS;
                })
                .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("dev/crash")
                          .withAction(context -> {
                              CrashHandler.getInstance().printCrashReport(new CrashReport("Test crash", new Throwable()));
                              return ModCommands.COMMAND_SUCCESS;
                          })
                .buildAndRegister();
        ModCommand.Builder.builder()
                .withPath("dev/gui/debug")
                .withAction(context -> {
                    var src = context.getSource();
                    if(ModRenderer.guiDebug()) {
                        src.sendSuccess(Component.literal("Enabled gui debug mode!"), false);
                    }
                    else {
                        src.sendSuccess(Component.literal("Disabled gui debug mode!"), false);
                    }
                    return ModCommands.COMMAND_SUCCESS;
                })
                .buildAndRegister();
    }
}
