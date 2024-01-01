package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * A sequence of {@link FormattedChar}.
 * This class is most accurate for providing format info for each char of this sequence.
 **/
public class FormattedCharSeq {

    public static final Supplier<FormattedCharSeq> EMPTY = FormattedCharSeq::new;

    private final List<FormattedChar> chars;


    public FormattedCharSeq() {
        this(new ArrayList<FormattedChar>());
    }


    public FormattedCharSeq(List<FormattedChar> chars) {
        this.chars = chars;
    }


    public FormattedCharSeq(FormattedString formattedString) {
        this(formattedString.getChars());
    }


    public FormattedCharSeq(FormattedChar... chars) {
        this(Lists.newArrayList(chars));
    }


    public FormattedCharSeq(Collection<FormattedString> strings) {
        this.chars = Lists.newArrayList();
        for(FormattedString string : strings) {
            this.chars.addAll(Arrays.asList(string.getChars()));
            this.chars.add(new FormattedChar('\n', string.formatting()));
        }
    }


    public static FormattedCharSeq of(String text, FontFormatting format) {
        return new FormattedCharSeq(new FormattedString(text, format));
    }


    public static FormattedCharSeq fromJson(JsonElement jsonElement) {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<FormattedChar> chars = Lists.newArrayListWithCapacity(jsonArray.size());
        for(JsonElement jsonElement1 : jsonArray) {
            chars.add(FormattedChar.fromJson(jsonElement1));
        }

        return new FormattedCharSeq(chars);
    }


    public void append(FormattedString formattedString) {
        this.chars.addAll(Arrays.asList(formattedString.getChars()));
    }


    public void append(FormattedCharSeq formattedCharSeq) {
        this.chars.addAll(formattedCharSeq.chars);
    }


    public void replace(int index, FormattedChar formattedChar) {
        this.chars.remove(index);
        this.insert(index, formattedChar);
    }


    public void insert(int index, FormattedChar formattedChar) {
        this.insertOrAdd(index, formattedChar);
    }


    private void insertOrAdd(int index, FormattedChar formattedChar) {
        if(index < this.chars.size()) {
            this.chars.add(index, formattedChar);
        }
        else {
            this.append(formattedChar);
        }
    }


    public void append(FormattedChar formattedChar) {
        this.chars.add(formattedChar);
    }


    public void replace(int startIndex, int endIndex, FormattedString formattedString) {
        this.delete(startIndex, endIndex);
        this.insert(startIndex, formattedString);
    }


    public void delete(int startIndex, int endIndex) {
        this.chars.subList(Math.max(startIndex, 0), Math.min(endIndex, this.chars.size())).clear();
    }


    public void insert(int beginIndex, FormattedString formattedString) {
        for(int i = beginIndex; i < beginIndex + formattedString.length(); i++) {
            this.insertOrAdd(i, formattedString.charAt(i));
        }
    }


    public void replace(int startIndex, int endIndex, FormattedCharSeq formattedCharSeq) {
        this.delete(startIndex, endIndex);
        this.insert(startIndex, formattedCharSeq);
    }


    public void insert(int beginIndex, FormattedCharSeq formattedCharSeq) {
        if(beginIndex < this.chars.size()) {
            this.chars.addAll(beginIndex, formattedCharSeq.chars);
        }
        else {
            this.chars.addAll(formattedCharSeq.chars);
        }
    }


    public int length() {
        return this.chars.size();
    }


    public float width() {
        float width = 0;
        for(FormattedChar formattedChar : this.chars) {
            if(!formattedChar.isLineBreakChar()) {
                width += formattedChar.getWidth();
            }
        }

        return width;
    }


    public FormattedChar charAt(int index) {
        return this.chars.get(index);
    }


    public boolean isEmpty() {
        return this.chars.isEmpty();
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.chars.forEach(formattedChar -> builder.append(formattedChar.character()));
        return builder.toString();
    }


    public float height(float maxWidth) {
        float height = 0;

        List<FormattedCharSeq> subs = new ArrayList<>();

        this.splitLines(maxWidth, (pos, contentWidth) -> subs.add(this.subSeq(pos, contentWidth)));

        for(FormattedCharSeq sub : subs) {
            height += sub.getMaxCharHeight();
        }

        return height;
    }



    public void splitLines(float maxWidth, LinePosConsumer consumer) {
        if(maxWidth <= 0) {
            throw new IllegalArgumentException("Max width must be positive and greater than zero!");
        }
        int j = 0;
        int i;
        float width = 0;
        int lastSpacePos = -1;
        for(i = j; i < this.chars.size(); i++) {
            FormattedChar formattedChar = this.chars.get(i);
            char c = formattedChar.character();

            if(Character.isWhitespace(c)) {
                lastSpacePos = i;
            }

            width += formattedChar.getWidth();

            if(width >= maxWidth || c == '\n') {
                int splitPos = ((lastSpacePos != -1) ? lastSpacePos : i - 1);
                consumer.accept(j, splitPos);

                width = 0;
                //Keep the width of the string that has been split
                for(int k = splitPos; k < Math.min(i, this.chars.size()); k++) {
                    FormattedChar formattedChar1 = this.chars.get(k);
                    width += formattedChar1.getWidth();
                }

                //Continue at the next char
                j = splitPos + 1;
                lastSpacePos = -1;
            }
        }
        consumer.accept(j, i);
    }


    public int getIndexAtWidth(float maxWidth) {
        float width = 0;
        for(int i = 0; i < this.chars.size(); i++) {
            FormattedChar formattedChar = this.chars.get(i);
            if(!formattedChar.isLineBreakChar()) {
                width += formattedChar.getWidth();
            }
            if(width >= maxWidth) {
                return i;
            }
        }
        return this.chars.size();
    }


    public FormattedCharSeq subSeq(int startIndex, int endIndex) {
        List<FormattedChar> chars1 = this.chars.subList(Mth.clamp(startIndex, 0, this.chars.size()), Mth.clamp(endIndex, 0, this.chars.size()));
        return new FormattedCharSeq(chars1);
    }


    public float getMaxCharHeight() {
        float height = 0;
        for(FormattedChar formattedChar : this.chars) {
            float charHeight = formattedChar.getHeight();
            if(charHeight > height) {
                height = charHeight;
            }
        }
        return height;
    }


    public void setForAll(Consumer<FontFormatting> formatConsumer) {
        for(FormattedChar formattedChar : this.chars) {
            formatConsumer.accept(formattedChar.format());
        }
    }


    public ImmutableList<FormattedChar> getChars() {
        return ImmutableList.copyOf(chars);
    }


    public JsonElement toJson() {
        JsonArray jsonArray = new JsonArray();
        for(FormattedChar formattedChar : this.chars) {
            jsonArray.add(formattedChar.toJson());
        }
        return jsonArray;
    }


    public float getMaxLineSpacing() {
        float height = 0;
        for(FormattedChar formattedChar : this.chars) {
            float lineSpacing = formattedChar.format().getLineSpacing();
            if(lineSpacing > height) {
                height = lineSpacing;
            }
        }
        return height;
    }


    @FunctionalInterface
    public interface LinePosConsumer {

        void accept(int beginIndex, int endIndex);
    }
}
