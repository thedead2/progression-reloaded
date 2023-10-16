package de.thedead2.progression_reloaded.client.gui.fonts.readers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.fonts.providers.SpaceProvider;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.Map;


public class SpecialReaders {
    public static final IFontReader<SpaceProvider> SPACE_READER = ((jsonObject, function) -> {
        Int2FloatMap int2FloatMap = new Int2FloatOpenHashMap();
        JsonObject jsonObject1 = GsonHelper.getAsJsonObject(jsonObject, "advances");

        for(Map.Entry<String, JsonElement> entry : jsonObject1.entrySet()) {
            int[] ints = entry.getKey().codePoints().toArray();
            if (ints.length != 1) {
                throw new JsonParseException("Expected single codepoint, got " + Arrays.toString(ints));
            }

            float f = GsonHelper.convertToFloat(entry.getValue(), "advance");
            int2FloatMap.put(ints[0], f);
        }

        return new SpaceProvider(int2FloatMap);
    });
}
