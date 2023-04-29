package de.thedead2.progression_reloaded.commands;

import de.thedead2.progression_reloaded.player.PlayerData;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;

public class PlayerCommands {

    public static void register(){
        ModCommand.Builder.newModCommand("players/team", context -> {
            var source = context.getSource();
            PlayerData playerData = PlayerDataHandler.getPlayerData().orElseThrow();
            var singlePlayer = playerData.getActivePlayer(source.getPlayerOrException());
            if(singlePlayer.isInTeam()){
                source.sendSuccess(TranslationKeyProvider.chatMessage("player_in_team", ChatFormatting.GREEN, singlePlayer.getTeam().get().getName()), false);
            }
            else source.sendFailure(TranslationKeyProvider.chatMessage("player_in_no_team"));
            return ModCommand.COMMAND_SUCCESS;
        });
    }
}
