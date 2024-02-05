package de.thedead2.progression_reloaded.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class AdminCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_QUEST = (context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(ModRegistries.QUESTS.get().getKeys().stream().map(ResourceLocation::toString), suggestionsBuilder);
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LEVEL = (context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(ModRegistries.LEVELS.get().getKeys().stream().map(ResourceLocation::toString), suggestionsBuilder);


    public static void register() {
        ModCommand.Builder.builder()
                          .withPath("award/quest/[quest]/[successful]") //TODO: Don't execute when command incomplete!
                          .withRequirement(commandSourceStack -> commandSourceStack.hasPermission(3))
                          .withArgument("[quest]", ResourceLocationArgument.id())
                          .withSuggestion("[quest]", SUGGEST_QUEST)
                          .withArgument("[successful]", BoolArgumentType.bool())
                          .withAction(context -> {
                              ResourceLocation quest_id = ResourceLocationArgument.getId(context, "quest");
                              boolean successful = BoolArgumentType.getBool(context, "successful");
                              LevelManager.getInstance().getQuestManager().award(quest_id, successful, PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException()));
                              context.getSource().sendSuccess(Component.literal("Completed quest: " + quest_id), false);
                              return ModCommands.COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("award/level/[level]")
                          .withRequirement(commandSourceStack -> commandSourceStack.hasPermission(3))
                          .withArgument("[level]", ResourceLocationArgument.id())
                          .withSuggestion("[level]", SUGGEST_LEVEL)
                          .withAction(context -> {
                              PlayerData player = PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException());
                              ResourceLocation levelId = ResourceLocationArgument.getId(context, "level");
                              ProgressionLevel level = ModRegistries.LEVELS.get().getValue(levelId);
                              if(level != null) {
                                  LevelManager.getInstance().award(player, level);
                                  context.getSource().sendSuccess(Component.literal("Completed level !"), false);
                                  return ModCommands.COMMAND_SUCCESS;
                              }
                              else {
                                  context.getSource().sendFailure(Component.literal("Unknown level !"));
                                  return ModCommands.COMMAND_FAILURE;
                              }
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("revoke/quest/[quest]")
                          .withRequirement(commandSourceStack -> commandSourceStack.hasPermission(3))
                          .withArgument("[quest]", ResourceLocationArgument.id())
                          .withSuggestion("[quest]", SUGGEST_QUEST)
                          .withAction(context -> {
                              ResourceLocation quest_id = ResourceLocationArgument.getId(context, "quest");
                              LevelManager.getInstance().getQuestManager().revoke(quest_id, PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException()));
                              context.getSource().sendSuccess(Component.literal("Revoked quest: " + quest_id), false);
                              return ModCommands.COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("revoke/level/[level]")
                          .withRequirement(commandSourceStack -> commandSourceStack.hasPermission(3))
                          .withArgument("[level]", ResourceLocationArgument.id())
                          .withSuggestion("[level]", SUGGEST_LEVEL)
                          .withAction(context -> {
                              PlayerData player = PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException());
                              ResourceLocation levelId = ResourceLocationArgument.getId(context, "level");
                              ProgressionLevel level = ModRegistries.LEVELS.get().getValue(levelId);
                              if(level != null) {
                                  LevelManager.getInstance().revoke(player, level);
                                  context.getSource().sendSuccess(Component.literal("Revoked level !"), false);
                                  return ModCommands.COMMAND_SUCCESS;
                              }
                              else {
                                  context.getSource().sendFailure(Component.literal("Unknown level !"));
                                  return ModCommands.COMMAND_FAILURE;
                              }
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("change/level/[level]")
                          .withRequirement(commandSourceStack -> commandSourceStack.hasPermission(3))
                          .withArgument("[level]", ResourceLocationArgument.id())
                          .withSuggestion("[level]", SUGGEST_LEVEL)
                          .withAction(context -> {
                              PlayerData player = PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException());
                              LevelManager.getInstance().updateLevel(player, ResourceLocationArgument.getId(context, "level"));
                              context.getSource().sendSuccess(Component.literal("Changed level!"), false);
                              return ModCommands.COMMAND_SUCCESS;
                          })
                          .buildAndRegister();

        ModCommand.Builder.builder()
                          .withPath("unlock/quest/[quest]")
                          .withArgument("[quest]", ResourceLocationArgument.id())
                          .withSuggestion("[quest]", SUGGEST_QUEST)
                          .withAction(context -> {
                              ResourceLocation quest_id = ResourceLocationArgument.getId(context, "quest");
                              PlayerData playerData = PlayerDataManager.getPlayerData(context.getSource().getPlayerOrException());

                              if(playerData.getPlayerQuests().getOrStartProgress(quest_id).unlock()) {
                                  context.getSource().sendSuccess(Component.literal("Unlocked quest " + quest_id), false);
                                  return ModCommands.COMMAND_SUCCESS;
                              }
                              else {
                                  context.getSource().sendFailure(Component.literal("Quest " + quest_id + " has already been unlocked!"));
                                  return ModCommands.COMMAND_FAILURE;
                              }
                          })
                          .buildAndRegister();
    }
}
