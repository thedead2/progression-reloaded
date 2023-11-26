package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import org.jetbrains.annotations.NotNull;


public record FormattedChar(char character, @NotNull FontFormatting format) {

    public static FormattedChar fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        char character = jsonObject.getAsJsonPrimitive("char").getAsCharacter();
        FontFormatting format = FontFormatting.fromJson(jsonObject.get("format"));
        return new FormattedChar(character, format);
    }


    public float getWidth() {
        ProgressionFont font = FontManager.getFont(this.format.getFont()).format(this.format);
        return font.charWidth(character);
    }


    public float getHeight() {
        return this.format.getLineHeight() + this.format.getLineSpacing();
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLineBreakChar() {
        return this.character == '\n';
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("char", this.character);
        jsonObject.add("format", this.format.toJson());

        return jsonObject;
    }
}
