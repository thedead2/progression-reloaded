package de.thedead2.progression_reloaded.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import de.thedead2.progression_reloaded.util.language.TranslationKeyType;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;


public class PRKeyMappings {

    private static final String CATEGORY_QUESTING = TranslationKeyProvider.translationKeyFor(TranslationKeyType.KEY, "categories", "questing");

    public static final KeyMapping FOLLOW_QUEST_KEY = create("follow_quest", GLFW.GLFW_KEY_V);

    public static final KeyMapping OPEN_QUEST_SCREEN_KEY = create("open_quest_screen", GLFW.GLFW_KEY_J, KeyConflictContext.IN_GAME);


    public static void register(RegisterKeyMappingsEvent event) {
        event.register(FOLLOW_QUEST_KEY);
        event.register(OPEN_QUEST_SCREEN_KEY);
    }


    public static String toString(KeyMapping key) {
        return key.getTranslatedKeyMessage().getString();
    }


    private static KeyMapping create(String name, int key) {
        return create(name, key, KeyConflictContext.UNIVERSAL);
    }


    private static KeyMapping create(String name, int key, IKeyConflictContext conflictContext) {
        return new KeyMapping(TranslationKeyProvider.translationKeyFor(TranslationKeyType.KEY, name), conflictContext, InputConstants.Type.KEYSYM, key, CATEGORY_QUESTING);
    }
}
