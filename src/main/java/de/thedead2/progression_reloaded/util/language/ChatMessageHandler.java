package de.thedead2.progression_reloaded.util.language;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ChatMessageHandler {

    public static void sendMessage(String messageKey, boolean literal, Player receiver, ChatFormatting... formats) {
        receiver.sendSystemMessage(literal ? Component.literal(messageKey).withStyle() : TranslationKeyProvider.chatMessage(messageKey, formats[0], receiver.getDisplayName()));
    }
}
