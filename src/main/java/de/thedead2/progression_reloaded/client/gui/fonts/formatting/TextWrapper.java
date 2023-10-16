package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;


public class TextWrapper {

    private final StringWidthProvider widthProvider;
    private final IntList breakPositions = new IntArrayList();


    public TextWrapper(StringWidthProvider widthProvider) {
        this.widthProvider = widthProvider;
    }


    public List<FormattedText> splitLines(net.minecraft.network.chat.FormattedText text, FontFormatting formatting, float maxWidth) {
        LineBreakFinder lineBreakFinder = new LineBreakFinder(maxWidth, formatting);
        TextCharIterator.iterate(text, lineBreakFinder);
        String newText = insertBreakChars(new StringBuilder(text.getString()));
        return splitAtLineBreakChars(new FormattedText(newText, new ResourceLocation("default"), new FontFormatting(), false));
    }
    public List<FormattedText> splitLines(String text, FontFormatting formatting, float maxWidth) {
        LineBreakFinder lineBreakFinder = new LineBreakFinder(maxWidth, formatting);
        TextCharIterator.iterate(text, lineBreakFinder);
        String newText = insertBreakChars(new StringBuilder(text));
        return splitAtLineBreakChars(new FormattedText(newText, new ResourceLocation("default"), new FontFormatting(), false));
    }

    public List<FormattedText> splitLines(FormattedCharSequence text, FontFormatting formatting, float maxWidth) {
        LineBreakFinder lineBreakFinder = new LineBreakFinder(maxWidth, formatting);
        StringBuilder builder = new StringBuilder();
        text.accept((pos, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return lineBreakFinder.visit(pos, codePoint);
        });
        String newText = insertBreakChars(builder);
        return splitAtLineBreakChars(new FormattedText(newText, new ResourceLocation("default"), new FontFormatting(), false));
    }

    private String insertBreakChars(StringBuilder text) {
        for (int breakPos : this.breakPositions) {
            if(text.charAt(breakPos + 1) == ' ') text.replace(breakPos + 1, breakPos + 2, "\n");
            else text.insert(breakPos + 1, '\n');
        }
        return text.toString();
    }

    public List<FormattedText> splitAtLineBreakChars(FormattedText text) {
        List<FormattedText> texts = Lists.newArrayList();
        MutableInt start = new MutableInt();
        MutableInt current = new MutableInt();
        TextCharIterator.iterate(text.text(), (charPos, codePoint) -> {
            current.setValue(charPos);
            if(codePoint == '\n' && current.intValue() != start.intValue()) {
                texts.add(text.subString(start.intValue(), current.intValue() - 1));
                start.setValue(charPos + 1);
            }
            return true;
        });

        texts.add(text.subString(start.intValue(), current.intValue()));

        return texts;
    }


    public float stringWidth(net.minecraft.network.chat.FormattedText text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        MutableFloat width = new MutableFloat();
        FloatList list = new FloatArrayList();
        TextCharIterator.iterate(text, (charPos, codePoint) -> {
            if(codePoint != '\n') width.add(this.widthProvider.getWidth(codePoint, formatting));
            else {
                list.add(width.floatValue());
                width.setValue(0);
            }
            return true;
        });

        Collections.sort(list);
        if(list.isEmpty()) return width.floatValue();
        else return list.getFloat(0);
    }


    public float stringWidth(FormattedCharSequence text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        MutableFloat width = new MutableFloat();
        FloatList list = new FloatArrayList();
        text.accept((charPos, style, codePoint) -> {
            if(codePoint != '\n') width.add(this.widthProvider.getWidth(codePoint, formatting));
            else {
                list.add(width.floatValue());
                width.setValue(0);
            }
            return true;
        });

        Collections.sort(list);
        if(list.isEmpty()) return width.floatValue();
        else return list.getFloat(0);
    }

    //FIXME: Wrong width when text is bold --> font used: minecraft:default
    //FIXME: Wrong width with expansiva font
    public float stringWidth(String text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        MutableFloat width = new MutableFloat();
        FloatList list = new FloatArrayList();
        TextCharIterator.iterate(text, (charPos, codePoint) -> {
            if(codePoint != '\n') width.add(this.widthProvider.getWidth(codePoint, formatting));
            else {
                list.add(width.floatValue());
                width.setValue(0);
            }
            return true;
        });

        Collections.sort(list);
        if(list.isEmpty()) return width.floatValue();
        else return list.getFloat(0);
    }

    /*//FIXME
    public float stringHeight(String text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        else {
            MutableInt mutableInt = new MutableInt();
            TextCharIterator.iterate(text, (charPos, codePoint) -> {
                if(codePoint == '\n') mutableInt.increment();
                return true;
            });
            return (formatting.getLineHeight() + formatting.getLineSpacing()) * (mutableInt.intValue() + 1);
        }
    }


    public float stringHeight(net.minecraft.network.chat.FormattedText text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        else {
            MutableInt mutableInt = new MutableInt();
            TextCharIterator.iterate(text, (charPos, codePoint) -> {
                if(codePoint == '\n') mutableInt.increment();
                return true;
            });
            return (formatting.getLineHeight() + formatting.getLineSpacing()) * (mutableInt.intValue() + 1);
        }
    }


    public float stringHeight(FormattedCharSequence text, FontFormatting formatting) {
        if(text == null) {
            return 0.0F;
        }
        else {
            MutableInt mutableInt = new MutableInt();
            text.accept((pos, style, codePoint) -> {
                if(codePoint == '\n') mutableInt.increment();
                return true;
            });
            return (formatting.getLineHeight() + formatting.getLineSpacing()) * (mutableInt.intValue() + 1);
        }
    }*/


    private class LineBreakFinder implements TextCharIterator.ICharVisitor {
        private final float maxLineWidth;
        private final FontFormatting formatting;
        private float width = 0;
        private int lastSpacePos = -1;

        private LineBreakFinder(float maxLineWidth, FontFormatting formatting) {
            this.maxLineWidth = Math.max(maxLineWidth, 1);
            this.formatting = formatting;
            TextWrapper.this.breakPositions.clear();
        }


        @Override
        public boolean visit(int charPos, int codePoint) {
            float f = TextWrapper.this.widthProvider.getWidth(codePoint, this.formatting);
            this.width += f;
            if(codePoint == 32) this.lastSpacePos = charPos;
            if(this.width > this.maxLineWidth) {
                TextWrapper.this.breakPositions.add(this.lastSpacePos != -1 ? this.lastSpacePos : charPos);
                this.width = 0;
            }
            return true;
        }
    }


    @FunctionalInterface
    public interface StringWidthProvider {
        float getWidth(int codePoint, FontFormatting formatting);
    }
}
