package de.thedead2.progression_reloaded.util.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.handler.FileHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.MarkerManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static de.thedead2.progression_reloaded.util.ModHelper.*;

public abstract class ModRegistries {
    private static final MarkerManager.Log4jMarker MARKER = new MarkerManager.Log4jMarker("ModRegistryManager");
    public static final Supplier<IForgeRegistry<ProgressionLevel>> LEVELS = DeferredRegisters.LEVEL_REGISTER.makeRegistry(() -> new RegistryBuilder<ProgressionLevel>().setDefaultKey(new ResourceLocation(MOD_ID, "default_level")));
    public static final Supplier<IForgeRegistry<ProgressionQuest>> QUESTS = DeferredRegisters.QUEST_REGISTER.makeRegistry(() -> new RegistryBuilder<ProgressionQuest>().setDefaultKey(new ResourceLocation(MOD_ID, "default_quest")));

    private static void load(){}

    public static void onMissingMappings(final MissingMappingsEvent event){
        var missingLevels = event.getMappings(RegistryKeys.LEVELS, MOD_ID);
        if(!missingLevels.isEmpty()) LOGGER.fatal(MARKER, "Detected missing progression levels, unable to load save data!");
        missingLevels.forEach(mapping -> {
            LOGGER.fatal(MARKER, mapping.getKey());
            mapping.fail();
        });
        var missingQuests = event.getMappings(RegistryKeys.QUESTS, MOD_ID);
        if(!missingQuests.isEmpty()) LOGGER.fatal(MARKER, "Detected missing progression quests, unable to load save data!");
        missingQuests.forEach(mapping -> {
            LOGGER.fatal(MARKER, mapping.getKey());
            mapping.fail();
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends ModRegistriesDynamicSerializer> void load(Path directoryPath, Class<T> type, DeferredRegister<T> deferredRegister) {
        final Map<ResourceLocation, T> temp = new HashMap<>();
        FileHandler.readDirectory(directoryPath.toFile(), directory -> {
            for (File file : Objects.requireNonNull(directory.listFiles(File::isFile))) {
                try {
                    JsonObject object = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                    ResourceLocation id = new ResourceLocation(object.get("id").getAsString());
                    T t = (T) type.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, object);
                    temp.put(id, t);
                } catch (Exception e) {
                    CrashHandler.getInstance().handleException("Failed to read file: " + file.getName(), e, Level.ERROR);
                }
            }
        });

        temp.forEach((resourceLocation, t) -> deferredRegister.register(resourceLocation.getPath(), () -> t));
    }

    public static void register(ProgressionLevel level){
        DeferredRegisters.LEVEL_REGISTER.register(level.getId().getPath(), () -> level);
    }

    public static void register(ProgressionQuest quest){
        DeferredRegisters.QUEST_REGISTER.register(quest.getId().getPath(), () -> quest);
    }

    private static void loadRegistries(){
        load(LEVELS_PATH, ProgressionLevel.class, DeferredRegisters.LEVEL_REGISTER);
        load(QUESTS_PATH, ProgressionQuest.class, DeferredRegisters.QUEST_REGISTER);
    }

    public static void saveRegistries() {
        save(LEVELS_PATH, LEVELS.get().getValues());
        save(QUESTS_PATH, QUESTS.get().getValues());
    }

    private static <T extends ModRegistriesDynamicSerializer> void save(Path directoryPath, Collection<T> buildObjects) {
        FileHandler.createDirectory(directoryPath.toFile());
        buildObjects.forEach(t -> {
            ByteArrayInputStream stream = new ByteArrayInputStream(t.toJson().toString().getBytes());
            try {
                FileHandler.writeFile(stream, directoryPath.resolve(t.getId().toString() + ".json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static abstract class DeferredRegisters {
        private static final DeferredRegister<ProgressionLevel> LEVEL_REGISTER = DeferredRegister.create(RegistryKeys.LEVELS, MOD_ID);
        private static final DeferredRegister<ProgressionQuest> QUEST_REGISTER = DeferredRegister.create(RegistryKeys.QUESTS, MOD_ID);
        public static void register(IEventBus bus) {
            load();
            LEVEL_REGISTER.register(bus);
            QUEST_REGISTER.register(bus);
        }

        static {
            loadRegistries();
        }
    }
}
