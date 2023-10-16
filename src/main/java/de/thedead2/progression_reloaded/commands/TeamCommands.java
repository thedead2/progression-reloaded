package de.thedead2.progression_reloaded.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.thedead2.progression_reloaded.commands.ModCommands.COMMAND_FAILURE;
import static de.thedead2.progression_reloaded.commands.ModCommands.COMMAND_SUCCESS;


public class TeamCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEAMS = (context, suggestionsBuilder) -> {
        Collection<PlayerTeam> teams = PlayerDataHandler.allTeams();
        return SharedSuggestionProvider.suggest(teams.stream().map(PlayerTeam::getId).map(ResourceLocation::toString), suggestionsBuilder);
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MEMBERS = (context, suggestionsBuilder) -> {
        ResourceLocation id = ResourceLocationArgument.getId(context, "team");
        var team = PlayerDataHandler.getTeam(id);

        Collection<KnownPlayer> members = team != null ? team.getMembers() : Collections.emptySet();
        return SharedSuggestionProvider.suggest(members.stream().map(KnownPlayer::name), suggestionsBuilder);
    };


    public static void register() {
        ModCommand.Builder.builder()
                          .withPath("teams/create/[name]")
                          .withArgument("[name]", StringArgumentType.greedyString())
                          .withAction(context -> {
                              var source = context.getSource();
                              String name = StringArgumentType.getString(context, "name");
                              ResourceLocation id = PlayerTeam.createId(name);
                              if(id == null) {
                                  source.sendFailure(TranslationKeyProvider.chatMessage("team_name_invalid", ChatFormatting.RED, name));
                                  return COMMAND_FAILURE;
                              }
                              PlayerDataHandler.getTeamData().ifPresent(teamData -> teamData.addTeam(new PlayerTeam(name, id, Collections.emptySet())));
                              source.sendSuccess(TranslationKeyProvider.chatMessage("team_created", ChatFormatting.GREEN, name), false);
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/delete/[id]")
                          .withArgument("[id]", ResourceLocationArgument.id())
                          .withSuggestion("[team]", SUGGEST_TEAMS)
                          .withAction(context -> {
                              var source = context.getSource();
                              ResourceLocation id = ResourceLocationArgument.getId(context, "id");
                              PlayerDataHandler.getTeamData().ifPresent(teamData -> {
                                  if(teamData.removeTeam(id)) {
                                      source.sendSuccess(TranslationKeyProvider.chatMessage("team_deleted", ChatFormatting.GREEN, id), false);
                                  }
                                  else {
                                      source.sendFailure(TranslationKeyProvider.chatMessage("unknown_team", ChatFormatting.RED, id));
                                  }
                              });
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/delete/all")
                          .withAction(context -> {
                              var source = context.getSource();
                              AtomicBoolean flag = new AtomicBoolean(false);
                              PlayerDataHandler.getTeamData().ifPresent(teamData -> {
                                  teamData.allTeams().forEach(teamData::removeTeam);
                                  flag.set(true);
                              });
                              if(flag.get()) {
                                  source.sendSuccess(TranslationKeyProvider.chatMessage("teams_deleted", ChatFormatting.GREEN), false);
                              }
                              else {
                                  source.sendFailure(TranslationKeyProvider.chatMessage("command_failed", ChatFormatting.RED));
                              }
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/[team]/add/[player]")
                          .withArgument("[team]", ResourceLocationArgument.id())
                          .withArgument("[player]", EntityArgument.players())
                          .withSuggestion("[team]", SUGGEST_TEAMS)
                          .withAction(context -> {
                              var source = context.getSource();
                              ResourceLocation id = ResourceLocationArgument.getId(context, "team");
                              var team = PlayerDataHandler.getTeam(id);
                              if(team == null) {
                                  source.sendFailure(TranslationKeyProvider.chatMessage("unknown_team", ChatFormatting.RED, id));
                                  return COMMAND_FAILURE;
                              }
                              var player = EntityArgument.getPlayers(context, "player");
                              Collection<KnownPlayer> players = new HashSet<>();
                              player.forEach(serverPlayer -> players.add(KnownPlayer.fromPlayer(serverPlayer)));

                              Set<KnownPlayer> invalidPlayers = new HashSet<>();
                              PlayerDataHandler.allTeams().forEach(team1 -> players.forEach(knownPlayer -> {
                                  if(team1.isPlayerInTeam(knownPlayer)) {
                                      invalidPlayers.add(knownPlayer);
                                      source.sendFailure(TranslationKeyProvider.chatMessage("player_already_in_other_team", ChatFormatting.RED, knownPlayer.name(), id));
                                  }
                              }));
                              players.removeAll(invalidPlayers);
                              team.addPlayers(players);
                              source.sendSuccess(TranslationKeyProvider.chatMessage("added_players", ChatFormatting.GREEN, players.size(), team.getName()), false);
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/[team]/remove/[player]")
                          .withArgument("[team]", ResourceLocationArgument.id())
                          .withArgument("[player]", EntityArgument.players())
                          .withSuggestion("[team]", SUGGEST_TEAMS)
                          .withSuggestion("[player]", SUGGEST_MEMBERS)
                          .withAction(context -> {
                              var source = context.getSource();
                              ResourceLocation id = ResourceLocationArgument.getId(context, "team");
                              var team = PlayerDataHandler.getTeam(id);
                              if(team == null) {
                                  source.sendFailure(TranslationKeyProvider.chatMessage("unknown_team", ChatFormatting.RED, id));
                                  return COMMAND_FAILURE;
                              }
                              var player = EntityArgument.getPlayers(context, "player");
                              Collection<KnownPlayer> players = new HashSet<>();
                              player.forEach(serverPlayer -> players.add(KnownPlayer.fromPlayer(serverPlayer)));
                              Set<KnownPlayer> unknownPlayers = new HashSet<>();
                              players.forEach(knownPlayer -> {
                                  if(!team.isPlayerInTeam(knownPlayer)) {
                                      source.sendFailure(TranslationKeyProvider.chatMessage("remove_unknown_team_member", ChatFormatting.RED, knownPlayer.name(), team.getName()));
                                      unknownPlayers.add(knownPlayer);
                                  }
                              });
                              players.removeAll(unknownPlayers);
                              team.removePlayers(players);
                              source.sendSuccess(TranslationKeyProvider.chatMessage("removed_players", ChatFormatting.GREEN, players.size(), team.getName()), false);
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/[team]/members")
                          .withArgument("[team]", ResourceLocationArgument.id())
                          .withSuggestion("[team]", SUGGEST_TEAMS)
                          .withAction(context -> {
                              var source = context.getSource();
                              ResourceLocation id = ResourceLocationArgument.getId(context, "team");
                              var team = PlayerDataHandler.getTeam(id);
                              if(team == null) {
                                  source.sendFailure(TranslationKeyProvider.chatMessage("unknown_team", ChatFormatting.RED, id));
                                  return COMMAND_FAILURE;
                              }
                              source.sendSuccess(TranslationKeyProvider.chatMessage("team_members", team.getName()), false);
                              team.getMembers().forEach(member -> source.sendSuccess(Component.literal(member.name()), false));
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("teams/all")
                          .withAction(context -> {
                              var source = context.getSource();
                              source.sendSuccess(TranslationKeyProvider.chatMessage("known_teams"), false);
                              PlayerDataHandler.allTeams().forEach(team -> source.sendSuccess(Component.literal(team.getName()), false));
                              return COMMAND_SUCCESS;
                          })
                          .buildAndRegister();
    }
}
