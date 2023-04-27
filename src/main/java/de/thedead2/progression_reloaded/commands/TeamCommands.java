package de.thedead2.progression_reloaded.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.progression_reloaded.data.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.SinglePlayer;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

import static de.thedead2.progression_reloaded.commands.ModCommand.COMMAND_FAILURE;
import static de.thedead2.progression_reloaded.commands.ModCommand.COMMAND_SUCCESS;

public class TeamCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEAMS = (context, suggestionsBuilder) -> {
        Collection<PlayerTeam> teams = PlayerDataHandler.getTeamData().orElseThrow().allTeams();
        return SharedSuggestionProvider.suggest(teams.stream().map(PlayerTeam::getTeamName), suggestionsBuilder);
    };

    public static void register(){
        ModCommand.Builder.newModCommand("teams/new/[name]", Map.of("[name]", StringArgumentType.string()), Collections.emptyMap(), context -> {
            var source = context.getSource();
            String name = StringArgumentType.getString(context, "name");
            ResourceLocation id = PlayerTeam.createId(name);
            if(id == null){
                source.sendFailure(TranslationKeyProvider.chatMessage("team_name_invalid", ChatFormatting.RED, name));
                return COMMAND_FAILURE;
            }
            PlayerDataHandler.getTeamData().ifPresent(teamData -> teamData.addTeam(new PlayerTeam(name, id, Collections.emptySet(), ProgressionLevel.lowest())));
            source.sendSuccess(TranslationKeyProvider.chatMessage("team_created", ChatFormatting.GREEN, name), false);
            return COMMAND_SUCCESS;
        });
        ModCommand.Builder.newModCommand("teams/[team]/add/[player]", Map.of("[team]", StringArgumentType.string(), "[player]", EntityArgument.players()), Map.of("[team]", SUGGEST_TEAMS), context -> {
            var source = context.getSource();
            String teamName = StringArgumentType.getString(context, "team_name");
            var team = PlayerDataHandler.getTeamData().orElseThrow().getTeam(PlayerTeam.createId(teamName));
            if(team == null){
                source.sendFailure(TranslationKeyProvider.chatMessage("unknown_team", ChatFormatting.RED, teamName));
                return COMMAND_FAILURE;
            }
            var player = EntityArgument.getPlayers(context, "player");
            Collection<SinglePlayer> players = new HashSet<>();
            player.forEach(serverPlayer -> {
                var singlePlayer = PlayerDataHandler.getPlayerData().orElseThrow().getActivePlayer(SinglePlayer.createId(serverPlayer.getStringUUID()));
                if(singlePlayer != null) players.add(singlePlayer);
                else source.sendFailure(TranslationKeyProvider.chatMessage("adding_failed", ChatFormatting.RED, serverPlayer.getName()));
            });
            team.addPlayers(players);
            source.sendSuccess(TranslationKeyProvider.chatMessage("added_players", ChatFormatting.GREEN, players.size()), false);
            return COMMAND_SUCCESS;
        });

        ModCommand.Builder.newModCommand("teams/all", context -> {
            var source = context.getSource();
            source.sendSuccess(TranslationKeyProvider.chatMessage("known_teams"), false);
            PlayerDataHandler.getTeamData().orElseThrow().allTeams().forEach(team -> source.sendSuccess(Component.literal(team.getTeamName()), false));
            return COMMAND_SUCCESS;
        });
    }
}
