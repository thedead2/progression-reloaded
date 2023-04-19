package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.handlers.RemappingHandler;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketReload;
import net.minecraft.command.ICommandSender;

@Command
public class CommandReload extends AbstractCommand {
    @Override
    public String getCommandName() {
        return "reload";
    }

    @Override
    public boolean processCommand(ICommandSender sender, String[] parameters) {
        if (sender.getEntityWorld().isRemote) {
            PacketHandler.sendToServer(new PacketReload());
        } else PacketReload.handle(JSONLoader.getServerTabData(RemappingHandler.getHostName()), false);

        return true;
    }
}
