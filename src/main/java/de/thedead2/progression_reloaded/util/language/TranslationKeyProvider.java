package de.thedead2.progression_reloaded.util.language;

import de.thedead2.progression_reloaded.util.handler.FileHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static de.thedead2.progression_reloaded.util.ModHelper.*;


public abstract class TranslationKeyProvider {

    private static final Set<String> usedTranslationKeys = new HashSet<>();


    public static Component chatMessage(String translationKeyName, Object... additionalArgs) {
        return chatMessage(translationKeyName, ChatFormatting.WHITE, additionalArgs);
    }


    public static Component chatMessage(String translationKeyName, ChatFormatting color, Object... additionalArgs) {
        Object[] additionalArgs2 = new Object[additionalArgs.length + 1];
        System.arraycopy(additionalArgs, 0, additionalArgs2, 1, additionalArgs.length);
        additionalArgs2[0] = "[" + MOD_NAME + "]: ";
        return newTranslatableComponent(translationKeyName, color, additionalArgs2);
    }


    public static Component newTranslatableComponent(String translationKey, ChatFormatting color, Object... additionalArgs) {
        return Component.translatable(chatTranslationKeyFor(translationKey), additionalArgs).withStyle(color);
    }


    public static String chatTranslationKeyFor(String name) {
        return translationKeyFor(TranslationKeyType.CHAT, name);
    }


    public static String translationKeyFor(TranslationKeyType type, String name) {
        return translationKeyFor(type, null, name);
    }


    public static String translationKeyFor(TranslationKeyType type, TranslationKeyType.TranslationKeySubType subType, String name) {
        String key = (type + "." + (subType != null ? subType + "." : "") + MOD_ID + "." + name).toLowerCase();
        usedTranslationKeys.add(key);
        return key;
    }


    public static Component chatLink(String link, ChatFormatting color) {
        return Component.literal(link).withStyle(ChatFormatting.UNDERLINE, color).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
    }


    public static void saveKeys() {
        if(!isDevEnv()) {
            return;
        }
        try {
            FileHandler.createDirectory(DIR_PATH.toFile());
            OutputStream out = Files.newOutputStream(DIR_PATH.resolve("langKeys.txt"));
            for(String s : usedTranslationKeys) {
                FileHandler.writeToFile(new ByteArrayInputStream((s + "\n").getBytes()), out);
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
