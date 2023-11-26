package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Unit;

import java.util.Optional;


public class TextCharIterator {
    private static final char REPLACEMENT_CHAR = '\ufffd';


    public static boolean iterate(FormattedText formattedText, FontFormatting format, ICharVisitor visitor) {
        return formattedText.visit(string -> iterate(FormattedCharSeq.of(string, format), visitor) ? Optional.empty() : Optional.of(Unit.INSTANCE)).isEmpty();
    }


    public static boolean iterate(FormattedCharSeq text, ICharVisitor visitor) {
        int i = text.length();

        for(int j = 0; j < i; ++j) {
            FormattedChar currentChar = text.charAt(j);
            if(Character.isHighSurrogate(currentChar.character())) {
                if (j + 1 >= i) {
                    if(!visitor.visit(j, currentChar.format(), REPLACEMENT_CHAR)) {
                        return false;
                    }
                    break;
                }

                FormattedChar nextChar = text.charAt(j + 1);
                if(Character.isLowSurrogate(nextChar.character())) {
                    if(!visitor.visit(j, currentChar.format(), Character.toCodePoint(currentChar.character(), nextChar.character()))) {
                        return false;
                    }

                    ++j;
                }
                else if(!visitor.visit(j, currentChar.format(), REPLACEMENT_CHAR)) {
                    return false;
                }
            } else if (!feedChar(j, currentChar, visitor)) {
                return false;
            }
        }

        return true;
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean feedChar(int charPos, FormattedChar formattedChar, ICharVisitor visitor) {
        return Character.isSurrogate(formattedChar.character()) ? visitor.visit(charPos, formattedChar.format(), REPLACEMENT_CHAR) : visitor.visit(charPos, formattedChar.format(), formattedChar.character());
    }


    public static boolean iterateBackwards(FormattedText formattedText, FontFormatting format, ICharVisitor visitor) {
        return formattedText.visit(string -> iterateBackwards(FormattedCharSeq.of(string, format), visitor) ? Optional.empty() : Optional.of(Unit.INSTANCE)).isEmpty();
    }


    public static boolean iterateBackwards(FormattedCharSeq text, ICharVisitor visitor) {
        int i = text.length();

        for(int j = i - 1; j >= 0; --j) {
            FormattedChar currentChar = text.charAt(j);
            if(Character.isLowSurrogate(currentChar.character())) {
                if (j - 1 < 0) {
                    if(!visitor.visit(0, currentChar.format(), REPLACEMENT_CHAR)) {
                        return false;
                    }
                    break;
                }

                FormattedChar previousChar = text.charAt(j - 1);
                if(Character.isHighSurrogate(previousChar.character())) {
                    --j;
                    if(!visitor.visit(j, currentChar.format(), Character.toCodePoint(previousChar.character(), currentChar.character()))) {
                        return false;
                    }
                }
                else if(!visitor.visit(j, currentChar.format(), REPLACEMENT_CHAR)) {
                    return false;
                }
            }
            else if(!feedChar(j, currentChar, visitor)) {
                return false;
            }
        }

        return true;
    }


    public static boolean iterate(FormattedString formattedString, ICharVisitor visitor) {
        return iterate(new FormattedCharSeq(formattedString), visitor);
    }


    public static boolean iterateBackwards(FormattedString formattedString, ICharVisitor visitor) {
        return iterateBackwards(new FormattedCharSeq(formattedString), visitor);
    }


    public static boolean iterate(String text, FontFormatting format, ICharVisitor visitor) {
        return iterate(FormattedCharSeq.of(text, format), visitor);
    }


    public static boolean iterateBackwards(String text, FontFormatting format, ICharVisitor visitor) {
        return iterateBackwards(FormattedCharSeq.of(text, format), visitor);
    }

    @FunctionalInterface
    public interface ICharVisitor {

        boolean visit(int charPos, FontFormatting format, int codePoint);
    }
}
