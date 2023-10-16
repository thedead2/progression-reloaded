package de.thedead2.progression_reloaded.commands;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.thedead2.progression_reloaded.api.command.CommandWrapperFunction;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.level.PistonEvent;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Predicate;


public record ModCommand(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder) { //TODO: better commands + new commands

    public static class Builder {
        private String commandPath;
        private final Map<String, ArgumentType<?>> arguments = new HashMap<>();
        private final Map<String, SuggestionProvider<CommandSourceStack>> suggestions = new HashMap<>();
        private Command<CommandSourceStack> command;
        private Predicate<CommandSourceStack> requirement;

        private Builder() {}

        public static Builder builder(){
            return new Builder();
        }

        public Builder withPath(String path){
            this.commandPath = path;
            return this;
        }

        public Builder withArgument(String name, ArgumentType<?> argument){
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(argument);

            this.arguments.put(name, argument);
            return this;
        }

        public Builder withSuggestion(String name, SuggestionProvider<CommandSourceStack> suggestion){
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(suggestion);

            this.suggestions.put(name, suggestion);
            return this;
        }

        public Builder withRequirement(Predicate<CommandSourceStack> requirement) {
            this.requirement = requirement;
            return this;
        }

        public Builder withAction(CommandWrapperFunction action) {
            this.command = (context) -> {
                try {
                    return action.runCommand(context);
                }
                catch(Throwable throwable) {
                    CrashHandler.getInstance().handleException("Something went wrong executing this command!", throwable, Level.ERROR);
                    context.getSource().sendFailure(TranslationKeyProvider.chatMessage("command_failed", ChatFormatting.RED));
                    return ModCommands.COMMAND_FAILURE;
                }
            };
            return this;
        }

        public void buildAndRegister() {
            ModCommands.registerCommand(this.build());
        }

        private ModCommand build() {
            Preconditions.checkNotNull(this.commandPath);
            Preconditions.checkArgument(!this.commandPath.isEmpty());
            Preconditions.checkNotNull(this.command);

            List<String> commandParts = this.resolveCommandParts();
            LiteralArgumentBuilder<CommandSourceStack> first = Commands.literal("pr");

            if(this.requirement != null) {
                first = first.requires(this.requirement);
            }

            LiteralArgumentBuilder<CommandSourceStack> command = commandParts.isEmpty() ? first.executes(this.command) : first.then(this.createCommandFromParts(first, commandParts));
            return new ModCommand(command);
        }

        private ArgumentBuilder<CommandSourceStack, ?> createCommandFromParts(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, List<String> commandParts) {
            ArgumentBuilder<CommandSourceStack, ?> argumentBuilder2 = argumentBuilder;

            if(!commandParts.isEmpty()) {
                String part = commandParts.get(0);
                if(part.startsWith("[") && part.endsWith("]")) {
                    if(arguments.isEmpty()) {
                        throw new NullPointerException("Can't create command argument because argument or suggestion is null!");
                    }

                    argumentBuilder2 = addArgument(part.substring(1, part.length() - 1), arguments.get(part), suggestions.get(part));
                }
                else {
                    argumentBuilder2 = Commands.literal(part);
                }
                List<String> subList = commandParts.subList(1, commandParts.size());
                if(!subList.isEmpty()) {
                    argumentBuilder2.then(createCommandFromParts(argumentBuilder2, subList));
                }
            }

            return argumentBuilder2.executes(command);
        }

        private List<String> resolveCommandParts() {
            List<String> commandParts = new ArrayList<>();

            if(commandPath.contains("/")) {
                String sub = commandPath.replace(commandPath.substring(commandPath.indexOf("/")), "");
                commandParts.add(sub);

                String nextSub = commandPath.replace(sub + "/", "");
                resolvePath(commandParts, nextSub);
            }
            else {
                commandParts.add(commandPath);
            }

            return commandParts;
        }


        private ArgumentBuilder<CommandSourceStack, ?> addCommandPart(String literal) {
            return Commands.literal(literal);
        }


        private ArgumentBuilder<CommandSourceStack, ?> addArgument(String name, ArgumentType<?> argumentType, SuggestionProvider<CommandSourceStack> suggestionProvider) {
            if(name == null || argumentType == null) {
                throw new NullPointerException("Can't create command argument because argument or suggestion is null!");
            }
            if(suggestionProvider == null) {
                return Commands.argument(name, argumentType);
            }
            else {
                return Commands.argument(name, argumentType).suggests(suggestionProvider);
            }
        }

        private void resolvePath(List<String> arguments, String pathIn) {
            if(pathIn.contains("/")) {
                String temp1 = pathIn.substring(pathIn.indexOf("/"));
                String temp2 = pathIn.replace(temp1 + "/", "");
                String temp3 = temp2.replace(temp2.substring(temp2.indexOf("/")), "");
                arguments.add(temp3);

                String next = pathIn.replace((temp3 + "/"), "");
                resolvePath(arguments, next);
            }
            else {
                arguments.add(pathIn);
            }
        }
    }
}
