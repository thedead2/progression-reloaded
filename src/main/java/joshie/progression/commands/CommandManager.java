package joshie.progression.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager extends CommandBase {
    public static final CommandManager INSTANCE = new CommandManager();
    private HashMap<String, AbstractCommand> commands = new HashMap();

    public void registerCommand(AbstractCommand command) {
        commands.put(command.getCommandName(), command);
    }

    public Map getCommands() {
        return commands;
    }

    List getPossibleCommands(ICommandSender sender) {
        ArrayList list = new ArrayList();
        for (AbstractCommand command: commands.values()) {
            if (sender.canCommandSenderUseCommand(command.getPermissionLevel().ordinal(), command.getCommandName())) {
                list.add(command);
            }
        }
        
        return list;
    }

    @Override
    public String getCommandName() {
        return "progression";
    }

    @SubscribeEvent
    public void onCommandSend(CommandEvent event) throws CommandException {
        if (event.getCommand() == this && event.getParameters().length > 0) {
            String commandName = event.getParameters()[0];
            AbstractCommand command = commands.get(commandName);
            if (command == null || !event.getSender().canCommandSenderUseCommand(command.getPermissionLevel().ordinal(), commandName)) {
                event.setCanceled(true);
            } else {
                processCommand(event, command);
            }
        }
    }

    //Attempt to process the command, throw wrong usage otherwise
    private void processCommand(CommandEvent event, AbstractCommand command) throws CommandException {
        String[] args = new String[event.getParameters().length - 1];
        System.arraycopy(event.getParameters(), 1, args, 0, args.length);
        if (!command.processCommand(event.getSender(), args)) {
            throwError(event.getSender(), command);
        }
    }
    
    static void throwError(ICommandSender sender, AbstractCommand command) {
        TextComponentTranslation chatcomponenttranslation1 = new TextComponentTranslation(getUsage(command), new Object[0]);
        chatcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
        sender.addChatMessage(chatcomponenttranslation1);
    }
    
    static String getUsage(AbstractCommand command) {
        return "/" + "progression" + " " + command.getCommandName() + " " + command.getUsage();
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return new ArrayList();
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] parameters) {
        if(parameters.length == 0) {
            throwError(sender, new CommandHelp());
        }
    } //Do sweet nothing

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}