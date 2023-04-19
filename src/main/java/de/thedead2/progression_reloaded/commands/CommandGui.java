package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.criteria.triggers.TriggerChangeGui;
import de.thedead2.progression_reloaded.helpers.ChatHelper;
import de.thedead2.progression_reloaded.network.PacketDisplayChat;
import de.thedead2.progression_reloaded.network.PacketHandler;
import net.minecraft.command.ICommandSender;

import static net.minecraft.util.text.TextFormatting.BLUE;

@Command
public class CommandGui extends AbstractCommand {
    @Override
    public String getCommandName() {
        return "gui";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public boolean processCommand(ICommandSender sender, String[] parameters) {
        if (sender.getEntityWorld().isRemote) {
            ChatHelper.displayChat(BLUE + de.thedead2.progression_reloaded.ProgressionReloaded.translate("display.gui") + " " + TriggerChangeGui.toggleDebug());
        } else PacketHandler.sendToClient(new PacketDisplayChat(BLUE + de.thedead2.progression_reloaded.ProgressionReloaded.translate("display.gui") + " " + TriggerChangeGui.toggleDebug()), sender);

        return true;
    }
}
