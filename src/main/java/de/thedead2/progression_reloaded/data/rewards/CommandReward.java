package de.thedead2.progression_reloaded.data.rewards;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;

public class CommandReward implements IReward{
    private final String command;

    public CommandReward(String command) {
        this.command = command;
    }

    @Override
    public void rewardPlayer(ServerPlayer player) {
        try {
            player.getServer().getFunctions().getDispatcher().execute(command, player.createCommandSourceStack());
        }
        catch (CommandSyntaxException e){
            CrashHandler.getInstance().handleException("Failed to execute command reward: " + command, e, Level.ERROR);
        }
    }
}
