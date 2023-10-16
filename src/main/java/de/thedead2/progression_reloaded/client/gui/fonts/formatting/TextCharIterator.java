package de.thedead2.progression_reloaded.client.gui.fonts.formatting;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Unit;

import java.util.Optional;


public class TextCharIterator {
    private static final char REPLACEMENT_CHAR = '\ufffd';

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean feedChar(int charPos, char character, ICharVisitor visitor) {
        return Character.isSurrogate(character) ? visitor.visit(charPos, REPLACEMENT_CHAR) : visitor.visit(charPos, character);
    }

    public static boolean iterate(FormattedText formattedText, ICharVisitor visitor) {
        return formattedText.visit(string -> iterate(string, visitor) ? Optional.empty() : Optional.of(Unit.INSTANCE)).isEmpty();
    }
    public static boolean iterateBackwards(FormattedText formattedText, ICharVisitor visitor) {
        return formattedText.visit(string -> iterateBackwards(string, visitor) ? Optional.empty() : Optional.of(Unit.INSTANCE)).isEmpty();
    }

    public static boolean iterate(String text, ICharVisitor visitor) {
        int i = text.length();

        for(int j = 0; j < i; ++j) {
            char currentChar = text.charAt(j);
            if (Character.isHighSurrogate(currentChar)) {
                if (j + 1 >= i) {
                    if (!visitor.visit(j, REPLACEMENT_CHAR)) {
                        return false;
                    }
                    break;
                }

                char nextChar = text.charAt(j + 1);
                if (Character.isLowSurrogate(nextChar)) {
                    if (!visitor.visit(j, Character.toCodePoint(currentChar, nextChar))) {
                        return false;
                    }

                    ++j;
                } else if (!visitor.visit(j, REPLACEMENT_CHAR)) {
                    return false;
                }
            } else if (!feedChar(j, currentChar, visitor)) {
                return false;
            }
        }

        return true;
    }

    public static boolean iterateBackwards(String text, ICharVisitor visitor) {
        int i = text.length();

        for(int j = i - 1; j >= 0; --j) {
            char character = text.charAt(j);
            if (Character.isLowSurrogate(character)) {
                if (j - 1 < 0) {
                    if (!visitor.visit(0, REPLACEMENT_CHAR)) {
                        return false;
                    }
                    break;
                }

                char c1 = text.charAt(j - 1);
                if (Character.isHighSurrogate(c1)) {
                    --j;
                    if (!visitor.visit(j, Character.toCodePoint(c1, character))) {
                        return false;
                    }
                } else if (!visitor.visit(j, REPLACEMENT_CHAR)) {
                    return false;
                }
            } else if (!feedChar(j, character, visitor)) {
                return false;
            }
        }

        return true;
    }

    @FunctionalInterface
    public interface ICharVisitor {
        boolean visit(int charPos, int codePoint);
    }
}
