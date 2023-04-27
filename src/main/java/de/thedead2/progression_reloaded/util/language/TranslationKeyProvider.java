package de.thedead2.progression_reloaded.util.language;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_NAME;

public abstract class TranslationKeyProvider {

    public static String chatTranslationKeyFor(String name){
        return translationKeyFor(TranslationKeyType.CHAT, name);
    }


    public static String translationKeyFor(TranslationKeyType type, String name){
        return translationKeyFor(type, null, name);
    }

    public static String translationKeyFor(TranslationKeyType type, TranslationKeyType.TranslationKeySubType subType, String name){
        String key = MOD_ID + "." + type + "." + (subType != null ? subType + "." : "") + name;
        return key.toLowerCase();
    }

    public static Component chatMessage(String translationKeyName, ChatFormatting color, Object... additionalArgs){
        Object[] additionalArgs2 = new Object[additionalArgs.length + 1];
        System.arraycopy(additionalArgs, 0, additionalArgs2, 1, additionalArgs.length);
        additionalArgs2[0] = "[" + MOD_NAME + "]: ";
        return newTranslatableComponent(translationKeyName, color, additionalArgs2);
    }

    public static Component newTranslatableComponent(String translationKey, ChatFormatting color, Object... additionalArgs){
        return Component.translatable(chatTranslationKeyFor(translationKey), additionalArgs).withStyle(color);
    }

    public static Component chatMessage(String translationKeyName, Object... additionalArgs){
        return chatMessage(translationKeyName, ChatFormatting.WHITE, additionalArgs);
    }

    public static Component chatLink(String link, ChatFormatting color){
        return Component.literal(link).withStyle(ChatFormatting.UNDERLINE, color).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
    }
}
