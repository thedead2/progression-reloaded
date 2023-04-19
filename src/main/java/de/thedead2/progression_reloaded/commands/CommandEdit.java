package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.lib.GuiIDs;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketOpenEditor;
import net.minecraft.command.ICommandSender;

@Command
public class CommandEdit extends AbstractCommand {
    @Override
    public String getCommandName() {
        return "edit";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public boolean processCommand(ICommandSender sender, String[] parameters) {
        if (sender.getEntityWorld().isRemote) {
            MCClientHelper.getPlayer().openGui(de.thedead2.progression_reloaded.ProgressionReloaded.instance, GuiIDs.EDITOR, null, 0, 0, 0);
        } else PacketHandler.sendToClient(new PacketOpenEditor(), sender);
        
        return true;
    }
}
