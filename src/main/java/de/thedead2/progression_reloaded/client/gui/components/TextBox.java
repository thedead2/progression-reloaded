package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.misc.ScrollBar;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedChar;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.fonts.rendering.FontRenderer;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class TextBox extends AbstractTextField {

    private final List<DisplayLine> displayLines;

    private Alignment.YAlign verticalAlignment = Alignment.YAlign.TOP;


    public TextBox(Area area) {
        this(area, FormattedString.EMPTY);
    }


    public TextBox(Area area, FormattedString val) {
        this(area, new FormattedCharSeq(val));
    }


    public TextBox(Area area, FormattedCharSeq val) {
        this(area, val, 0);
    }


    public TextBox(Area area, FormattedCharSeq val, int borderColor) {
        super(area, ScrollDirection.VERTICAL, ScrollBar.Visibility.IF_NECESSARY, borderColor);
        this.displayLines = new ArrayList<>();

        this.setValue(val);
    }


    public TextBox(Area area, int borderColor) {
        this(area, FormattedString.EMPTY, borderColor);
    }


    public TextBox(Area area, FormattedString val, int borderColor) {
        this(area, new FormattedCharSeq(val), borderColor);
    }


    public TextBox(Area area, List<FormattedString> strings) {
        this(area, new FormattedCharSeq(strings));
    }


    public TextBox(Area area, List<FormattedString> strings, int borderColor) {
        this(area, new FormattedCharSeq(strings), borderColor);
    }


    public void setVerticalTextAlignment(Alignment.YAlign verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }


    @Override
    public void renderContents(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.value.isEmpty() && this.suggestion != null) {
            FontRenderer.draw(poseStack, this.suggestion, this.contentArea.getInnerX(), this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight())), this.contentArea.getZ());
        }
        else {
            for(int i = 0; i < this.displayLines.size(); i++) {
                float yPos = this.getYPosByLineIndex(i);
                DisplayLine line = this.displayLines.get(i);
                FormattedCharSeq lineContent = line.getContent();
                if(!this.withinContentAreaTopBottom(yPos, yPos + lineContent.getMaxCharHeight())) {
                    continue;
                }

                float xPos = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), lineContent.width());

                FontRenderer.draw(poseStack, lineContent, xPos, yPos, this.contentArea.getZ());
            }
        }

        if(this.hasSelection() && this.isEditable()) {
            StringSelection selection = this.getSelected();
            for(int i = this.getLineIndexByCharPos(selection.beginIndex); i < Mth.clamp(this.getLineIndexByCharPos(selection.endIndex) + 1, 0, this.displayLines.size()); i++) {
                DisplayLine line = this.displayLines.get(i);
                FormattedCharSeq lineContent = line.getContent();
                float yStart = this.getYPosByLineIndex(i) - 1;
                float yStop = yStart + lineContent.getMaxCharHeight() + 2;

                if(!this.withinContentAreaTopBottom(yStart, yStop)) {
                    continue;
                }

                float lineWidth = lineContent.width();

                float xStart = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), lineWidth) - 1;
                if(line.contains(selection.beginIndex)) {
                    xStart += this.value.subSeq(line.beginIndex, selection.beginIndex).width();
                }
                float xStop = line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), lineWidth) + lineWidth;
                if(line.contains(selection.endIndex)) {
                    xStop -= this.value.subSeq(selection.endIndex, line.endIndex).width();
                }

                this.renderHighlight(poseStack, xStart, yStart, xStop, yStop);
            }
        }

        this.renderCursor(poseStack, mouseX, mouseY, partialTick);
    }


    protected float contentHeight() {
        float height = 0;
        for(DisplayLine line : this.displayLines) {
            FormattedCharSeq lineContent = line.getContent();
            height += lineContent.isEmpty() ? (this.cursor.getFormat().getLineHeight() + this.cursor.getFormat().getLineSpacing()) : lineContent.getMaxCharHeight();
        }
        return height;
    }


    @Override
    public void seekCursorToPoint(double x, double y) {
        int j = this.getLineIndexByYPos(y);
        DisplayLine line = this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
        float widthDif = (float) (x - line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), line.getContent().width()));
        FormattedCharSeq lineContent = line.getContent();
        int i = lineContent.getIndexAtWidth(widthDif);
        this.changeCursorPosition(Whence.ABSOLUTE, line.beginIndex + i);
    }


    @Override
    protected void onValueChange() {
        this.recalculateDisplayLines();
        super.onValueChange();
    }


    private void recalculateDisplayLines() {
        this.displayLines.clear();
        if(this.value.isEmpty()) {
            this.displayLines.add(new DisplayLine(0, 0, Alignment.XAlign.LEFT));
        }
        else {
            this.value.splitLines(this.contentArea.getInnerWidth(), (i, j) -> this.displayLines.add(new DisplayLine(i, j, this.cursor.getFormat().getTextAlignment().getXAlign())));
        }
    }


    @Override
    protected void updateCursorDisplayPos() {
        DisplayLine line = this.getCursorLine();
        this.cursor.setDisplayPos(line.textAlignment.getXPos(this.contentArea.getInnerX(), this.contentArea.getInnerWidth(), line.getContent().width()) + this.value.subSeq(line.beginIndex, this.cursor.getCharPos())
                                                                                                                                                                    .width() - (this.cursor.getLineWidth() / 2), this.getYPosByLineIndex(this.displayLines.indexOf(line)), this.contentArea.getZ());
    }


    private DisplayLine getLineWithOffsetFromCursorLine(int offset) {
        int i = this.getCursorLineIndex();
        if(i < 0) {
            throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor.getCharPos() + ", length = " + this.value.length() + ")");
        }
        else {
            return this.displayLines.get(Mth.clamp(i + offset, 0, this.displayLines.size() - 1));
        }
    }


    public int getCursorLineIndex() {
        return this.getLineIndexByCharPos(this.cursor.getCharPos());
    }


    protected int getLineIndexByCharPos(int charPos) {
        for(int i = 0; i < this.displayLines.size(); ++i) {
            DisplayLine line = this.displayLines.get(i);
            if(line.contains(charPos)) {
                return i;
            }
        }

        return -1;
    }


    @Override
    protected double scrollRate() {
        return 9.0D / 2.0D;
    }


    protected float getYPosByLineIndex(int index) {
        float yStart = this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight()));

        for(int i = 0; i < Mth.clamp(index, 0, this.displayLines.size()); i++) {
            DisplayLine line = this.displayLines.get(i);
            FormattedCharSeq lineContent = line.getContent();
            float lineHeight = lineContent.getMaxCharHeight();
            yStart += lineHeight;
        }

        return yStart;
    }


    @Override
    protected void scrollToCursor() { //FIXME: When text gets deleted
        double amount = this.yScrollBar.getScrollAmount();

        int i = this.getCursorLineIndex();
        FormattedCharSeq cursorLineContent = this.getCursorLine().getContent();
        float contentHeight = cursorLineContent.isEmpty() ? this.cursor.getFormat().getLineHeight() : cursorLineContent.getMaxCharHeight();
        float yPos = this.getYPosByLineIndex(i);
        if(yPos + contentHeight - amount >= this.contentArea.getInnerYMax()) {
            amount = yPos + contentHeight - this.contentArea.getInnerYMax();
        }
        else if(yPos <= this.contentArea.getInnerY() + amount) {
            amount = yPos - this.contentArea.getInnerY();
        }

        this.yScrollBar.setScrollAmount(amount);
    }


    @Override
    protected void onTab() {
        this.insertText(new FormattedString("    ", this.cursor.getFormat()));
    }


    public int getLineCount() {
        return this.displayLines.size();
    }


    public int getSelectionLineIndex() {
        return this.getLineIndexByCharPos(this.cursor.getSelectPos());
    }


    protected DisplayLine getLine(int index) {
        return this.displayLines.get(Mth.clamp(index, 0, this.displayLines.size() - 1));
    }


    public void changeCursorLinePos(int offset) {
        if(offset != 0) {
            DisplayLine lineAtOffset = this.getLineWithOffsetFromCursorLine(offset);
            DisplayLine currentCursorLine = this.getCursorLine();

            int dif = this.value.subSeq(currentCursorLine.beginIndex, this.cursor.getCharPos()).length();
            this.changeCursorPosition(Whence.ABSOLUTE, lineAtOffset.beginIndex + Math.min(dif, lineAtOffset.endIndex - lineAtOffset.beginIndex));
        }
    }


    @Override
    protected void onEnter() {
        this.insertChar(new FormattedChar('\n', this.cursor.getFormat()));
    }


    @Override
    protected DisplayLine getCursorLine() {
        return this.getLineWithOffsetFromCursorLine(0);
    }


    @Override
    protected void onUp() {
        this.changeCursorLinePos(-1);
    }


    @Override
    protected void onDown() {
        this.changeCursorLinePos(1);
    }


    protected int getLineIndexByYPos(double y) {
        float yStart = (float) (this.verticalAlignment.getYPos(this.contentArea.getInnerY(), this.contentArea.getInnerHeight(), Mth.clamp(this.contentHeight(), 1, this.contentArea.getInnerHeight())) - this.yScrollBar.getScrollAmount());

        if(y < yStart) {
            return 0;
        }

        for(int i = 0; i < this.displayLines.size(); i++) {
            DisplayLine line = this.displayLines.get(i);
            FormattedCharSeq lineContent = line.getContent();
            float lineHeight = lineContent.getMaxCharHeight();
            if(y >= yStart && y < yStart + lineHeight) {
                return i;
            }
            yStart += lineHeight;
        }
        return this.displayLines.size();
    }


    protected Iterable<DisplayLine> iterateLines() {
        return this.displayLines;
    }

    protected class DisplayLine extends StringSelection {

        private final Alignment.XAlign textAlignment;


        DisplayLine(int beginIndex, int endIndex, Alignment.XAlign textAlignment) {
            super(beginIndex, endIndex);
            this.textAlignment = textAlignment;
        }
    }
}
