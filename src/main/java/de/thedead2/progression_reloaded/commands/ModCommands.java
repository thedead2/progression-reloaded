package de.thedead2.progression_reloaded.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.ArrayList;
import java.util.List;

import static de.thedead2.progression_reloaded.util.ModHelper.LOGGER;


public class ModCommands {

    public static final int COMMAND_FAILURE = -1;

    public static final int COMMAND_SUCCESS = 1;

    private static final List<ModCommand> COMMANDS = new ArrayList<>();


    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LOGGER.debug("Registering commands...");

        TeamCommands.register();
        AdminCommands.register();
        DevCommands.register();

        COMMANDS.forEach(modCommand -> dispatcher.register(modCommand.literalArgumentBuilder()));
        ConfigCommand.register(dispatcher);
        LOGGER.debug("Command registration complete.");
    }


    public static void registerCommand(ModCommand command) {
        COMMANDS.add(command);
    }
}
