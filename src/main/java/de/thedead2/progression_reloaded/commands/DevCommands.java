package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.components.toasts.ProgressToast;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.network.packets.ClientOnProgressChangedPacket;
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
                          .withPath("dev/lives/add")
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

        ModCommand.Builder.builder()
                          .withPath("dev/gui/toast")
                          .withAction(context -> {
                              ProgressToast toast = GuiFactory.createProgressToast(TestQuests.TEST2.getDisplay(), ClientOnProgressChangedPacket.Type.QUEST_COMPLETE);
                              ModClientInstance.getInstance().getModRenderer().getToastRenderer().forceDisplayToast(toast);
                              return ModCommands.COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("dev/updateStatus")
                          .withAction(context -> {
                              context.getSource().sendSuccess(Component.literal("Force updating all level and quest status!"), true);
                              LevelManager.getInstance().updateStatus();
                              return ModCommands.COMMAND_SUCCESS;
                          })
                          .buildAndRegister();
    }
}
