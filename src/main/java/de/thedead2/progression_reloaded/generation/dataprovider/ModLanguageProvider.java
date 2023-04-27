package de.thedead2.progression_reloaded.generation.dataprovider;

import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import de.thedead2.progression_reloaded.util.language.TranslationKeyType;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.HashMap;
import java.util.Map;

public class ModLanguageProvider extends LanguageProvider {

    private final String lang;
    private final Map<String, String> keyMap = new HashMap<>();

    public ModLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
        this.lang = locale;
    }

    @Override
    protected void addTranslations() {
        this.gatherKeyMapData();
        keyMap.forEach(this::add);
    }

    private void gatherKeyMapData(){
            if (lang.equals("en_us")) {
                keyMap.put("item.progression_reloaded.progression_book", "Progression Book");
                keyMap.put(TranslationKeyProvider.translationKeyFor(TranslationKeyType.CREATIVE_MODE_TAB, "progression_reloaded_tab"), "Progression Reloaded");
            }
    }
}
