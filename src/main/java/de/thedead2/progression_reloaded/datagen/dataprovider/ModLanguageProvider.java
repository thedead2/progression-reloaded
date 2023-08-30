package de.thedead2.progression_reloaded.datagen.dataprovider;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.thedead2.progression_reloaded.util.ModHelper.DIR_PATH;
import static de.thedead2.progression_reloaded.util.language.TranslationKeyProvider.translationKeyFor;
import static de.thedead2.progression_reloaded.util.language.TranslationKeyType.*;


public class ModLanguageProvider extends LanguageProvider {

    private final String lang;

    private final Map<String, String> keyMap = new HashMap<>();

    private final Set<String> requiredLangKeys = new HashSet<>();


    public ModLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
        this.lang = locale;
        try {
            requiredLangKeys.addAll(Files.lines(DIR_PATH.resolve("langKeys.txt"), StandardCharsets.UTF_8).toList());
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void addTranslations() {
        this.gatherKeyMapData();
        //if (new ArrayList<>(keyMap.keySet()).containsAll(requiredLangKeys))
        keyMap.forEach(this::add);
        //else throw new IllegalStateException("Missing lang keys!");
    }


    private void gatherKeyMapData() {
        if(lang.equals("en_us")) {
            keyMap.put(translationKeyFor(ITEM, "progression_book"), "Progression Book");
            keyMap.put(translationKeyFor(CREATIVE_MODE_TAB, "progression_reloaded_tab"), "Progression Reloaded");
            keyMap.put(translationKeyFor(CHAT, "known_teams"), "%s All known teams:");
            keyMap.put(translationKeyFor(CHAT, "team_members"), "%s All team members of %s:");
            keyMap.put(translationKeyFor(CHAT, "unknown_team"), "%s There's no team with name %s!");
            keyMap.put(translationKeyFor(CHAT, "removed_players"), "%s Removed %s player(s) from %s");
            keyMap.put(translationKeyFor(CHAT, "added_players"), "%s Added %s player(s) to %s");
            keyMap.put(translationKeyFor(CHAT, "team_deleted"), "%s Team '%s' has been deleted!");
            keyMap.put(translationKeyFor(CHAT, "team_name_invalid"), "%s The specified team name '%s' is invalid!");
            keyMap.put(translationKeyFor(CHAT, "team_created"), "%s Team '%s' has been created successfully!");
            keyMap.put(translationKeyFor(CHAT, "command_failed"), "%s Failed to execute the command!");
            keyMap.put(translationKeyFor(CHAT, "remove_unknown_team_member"), "%s Can't remove '%s' from '%s' as player is not a member of that team!");
            keyMap.put(translationKeyFor(CHAT, "player_already_in_other_team"), "%s Can't add %s to %s as player is already a member of another team!");
            keyMap.put(translationKeyFor(CHAT, "player_in_team"), "%s You are a member of team '%s'!");
            keyMap.put(translationKeyFor(CHAT, "player_in_no_team"), "%s You are not a member of a team!");
        }
    }
}
