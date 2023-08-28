package de.thedead2.progression_reloaded.data.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;


public class CommandReward implements IReward {

    public static final ResourceLocation ID = IReward.createId("command");

    private final String command;


    public CommandReward(String command) {
        this.command = command;
    }


    public static CommandReward fromJson(JsonElement jsonElement) {
        return new CommandReward(jsonElement.getAsString());
    }


    @Override
    public void rewardPlayer(ServerPlayer player) {
        try {
            player.getServer().getFunctions().getDispatcher().execute(command, player.createCommandSourceStack());
        }
        catch(CommandSyntaxException e) {
            CrashHandler.getInstance().handleException("Failed to execute command reward: " + command, e, Level.ERROR);
        }
    }


    @Override
    public ResourceLocation getId() {
        return ID;
    }


    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.command);
    }
}
