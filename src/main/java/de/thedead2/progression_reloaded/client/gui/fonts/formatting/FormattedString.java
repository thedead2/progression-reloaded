package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FormattedString {

    public static final FormattedString EMPTY = new FormattedString("", FontFormatting.defaultFormatting());

    private final String text;

    private final FontFormatting formatting;

    @Nullable
    private ProgressionFont cachedFont;


    public FormattedString(Component text, ResourceLocation font, FontFormatting formatting, boolean withShadow) {
        this(text.getString(), font, formatting, withShadow);
    }


    public FormattedString(String text, ResourceLocation font, FontFormatting formatting, boolean withShadow) {
        this(text, formatting.setFont(font).setWithShadow(withShadow));
    }


    public FormattedString(String text, FontFormatting formatting) {
        this.text = text;
        this.formatting = formatting;
    }


    public FormattedString(Component text, FontFormatting formatting) {
        this(text.getVisualOrderText(), formatting);
    }


    public FormattedString(FormattedCharSequence text, FontFormatting formatting) {
        this(TextWrapper.getContent(text), formatting);
    }


    public static FormattedString fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String text = jsonObject.get("text").getAsString();
        FontFormatting formatting = FontFormatting.fromJson(jsonObject.get("format"));

        return new FormattedString(text, formatting);
    }


    public String text() {
        return text;
    }


    public float height(float maxWidth) {
        this.ensureCached();
        return this.cachedFont.height(this.text, maxWidth);
    }


    private void ensureCached() {
        if(this.cachedFont == null) {
            this.cachedFont = FontManager.getFont(this.font()).format(this.formatting);
        }
    }


    public ResourceLocation font() {
        return formatting.getFont();
    }


    public FormattedString subString(int start, int end) {
        if(!this.text.isEmpty()) {
            String sub = this.text.substring(start, end);
            return new FormattedString(sub, this.font(), this.formatting, this.withShadow());
        }
        else {
            return this;
        }
    }


    public boolean withShadow() {
        return formatting.isWithShadow();
    }


    public boolean isEmpty() {
        return this.text.isEmpty();
    }


    public int length() {
        return this.text.length();
    }


    public void draw(PoseStack poseStack, float xPos, float yPos, float zPos) {
        this.ensureCached();

        if(this.withShadow()) {
            this.cachedFont.drawShadow(poseStack, this.text, xPos, yPos, zPos);
        }
        else {
            this.cachedFont.draw(poseStack, this.text, xPos, yPos, zPos);
        }
    }


    public void drawWithLineWrap(PoseStack poseStack, float xPos, float yPos, float zPos, float maxWidth) {
        this.ensureCached();

        if(this.withShadow()) {
            this.cachedFont.drawShadowWithLineWrap(poseStack, this.text, formattedText -> this.formatting().getTextAlignment().getXPos(xPos, maxWidth, formattedText.width(), 0), yPos, zPos, maxWidth);
        }
        else {
            this.cachedFont.drawWithLineWrap(poseStack, this.text, formattedText -> this.formatting().getTextAlignment().getXPos(xPos, maxWidth, formattedText.width(), 0), yPos, zPos, maxWidth);
        }
    }


    public FontFormatting formatting() {
        return formatting;
    }


    public float width() {
        this.ensureCached();
        return this.cachedFont.width(this.text);
    }


    public FormattedString subStringByWidth(float maxWidth) {
        StringBuilder builder = new StringBuilder();
        char[] chars = new char[this.text.length()];
        this.text.getChars(0, this.text.length(), chars, 0);
        float width = 0;
        this.ensureCached();
        for(char c : chars) {
            float charWidth = this.cachedFont.charWidth(c);
            width += charWidth;
            builder.append(c);
            if(width >= maxWidth) {
                break;
            }
        }
        return new FormattedString(builder.toString(), this.font(), this.formatting(), this.withShadow());
    }


    public List<FormattedString> splitForWidth(float maxWidth) {
        this.ensureCached();
        return this.cachedFont.getTextWrapper().splitLines(this.text, this.font(), this.formatting(), this.withShadow(), maxWidth);
    }


    public FormattedChar[] getChars() {
        FormattedChar[] chars = new FormattedChar[this.text.length()];
        for(int i = 0; i < this.text.length(); i++) {
            chars[i] = new FormattedChar(this.text.charAt(i), this.formatting);
        }
        return chars;
    }


    public FormattedChar charAt(int index) {
        return new FormattedChar(this.text.charAt(index), this.formatting);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("text", this.text);
        jsonObject.add("format", this.formatting.toJson());

        return jsonObject;
    }
}
