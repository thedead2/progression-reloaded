package de.thedead2.progression_reloaded.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;


public class PlayerCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEST = (context, suggestionsBuilder) ->
            SharedSuggestionProvider.suggest(ModRegistries.QUESTS.get().getKeys().stream().map(ResourceLocation::toString), suggestionsBuilder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LEVEL = (context, suggestionsBuilder) ->
            SharedSuggestionProvider.suggest(ModRegistries.LEVELS.get().getKeys().stream().map(ResourceLocation::toString), suggestionsBuilder
            );


    public static void register() {
        ModCommand.Builder.newModCommand("players/team", context -> {
            var source = context.getSource();
            SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(source.getPlayerOrException());
            if(singlePlayer.isInTeam()) {
                source.sendSuccess(TranslationKeyProvider.chatMessage("player_in_team", ChatFormatting.GREEN, singlePlayer.getTeam().get().getName()), false);
            }
            else {
                source.sendFailure(TranslationKeyProvider.chatMessage("player_in_no_team"));
            }
            return ModCommand.COMMAND_SUCCESS;
        });
        ModCommand.Builder.newModCommand("players/award/quest/[quest]", Map.of("[quest]", ResourceLocationArgument.id()), Map.of("[quest]", SUGGEST_QUEST),
                                         context -> {
                                             ResourceLocation quest_id = ResourceLocationArgument.getId(context, "quest");
                                             LevelManager.getInstance().getQuestManager().award(quest_id, KnownPlayer.fromPlayer(context.getSource().getPlayerOrException()));
                                             context.getSource().sendSuccess(Component.literal("Completed quest: " + quest_id), false);
                                             return ModCommand.COMMAND_SUCCESS;
                                         }
        );
        ModCommand.Builder.newModCommand("players/revoke/quest/[quest]", Map.of("[quest]", ResourceLocationArgument.id()), Map.of("[quest]", SUGGEST_QUEST),
                                         context -> {
                                             ResourceLocation quest_id = ResourceLocationArgument.getId(context, "quest");
                                             LevelManager.getInstance().getQuestManager().revoke(quest_id, KnownPlayer.fromPlayer(context.getSource().getPlayerOrException()));
                                             context.getSource().sendSuccess(Component.literal("Revoked quest: " + quest_id), false);
                                             return ModCommand.COMMAND_SUCCESS;
                                         }
        );
        ModCommand.Builder.newModCommand("players/level", context -> {
            SinglePlayer player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
            context.getSource().sendSuccess(Component.literal("Your current level is: " + player.getProgressionLevel().getTitle()), false);
            return ModCommand.COMMAND_SUCCESS;
        });

        ModCommand.Builder.newModCommand("players/award/level/[level]", Map.of("[level]", ResourceLocationArgument.id()), Map.of("[level]", SUGGEST_LEVEL),
                                         context -> {
                                             SinglePlayer player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
                                             LevelManager.getInstance().award(player, ResourceLocationArgument.getId(context, "level"));
                                             context.getSource().sendSuccess(Component.literal("Completed level !"), false);
                                             return ModCommand.COMMAND_SUCCESS;
                                         }
        );
        ModCommand.Builder.newModCommand("players/revoke/level/[level]", Map.of("[level]", ResourceLocationArgument.id()), Map.of("[level]", SUGGEST_LEVEL),
                                         context -> {
                                             SinglePlayer player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
                                             LevelManager.getInstance().revoke(player, ResourceLocationArgument.getId(context, "level"));
                                             context.getSource().sendSuccess(Component.literal("Revoked level!"), false);
                                             return ModCommand.COMMAND_SUCCESS;
                                         }
        );
        ModCommand.Builder.newModCommand("players/change/level/[level]", Map.of("[level]", ResourceLocationArgument.id()), Map.of("[level]", SUGGEST_LEVEL),
                                         context -> {
                                             SinglePlayer player = PlayerDataHandler.getActivePlayer(context.getSource().getPlayerOrException());
                                             LevelManager.getInstance().updateLevel(player, ResourceLocationArgument.getId(context, "level"));
                                             context.getSource().sendSuccess(Component.literal("Changed level!"), false);
                                             return ModCommand.COMMAND_SUCCESS;
                                         }
        );
    }
}
