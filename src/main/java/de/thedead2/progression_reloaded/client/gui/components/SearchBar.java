package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.misc.ScrollBar;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.fonts.rendering.FontRenderer;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.gui.components.Whence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public class SearchBar extends AbstractTextField {

    private static final Alignment ALIGNMENT = Alignment.LEFT_CENTERED;

    @Nullable
    private Consumer<FormattedCharSeq> enterListener;


    public SearchBar(Area area) {
        this(area, FormattedString.EMPTY);
    }


    public SearchBar(Area area, FormattedString val) {
        this(area, new FormattedCharSeq(val));
    }


    public SearchBar(Area area, FormattedCharSeq val) {
        this(area, val, 0);
    }


    public SearchBar(Area area, FormattedCharSeq val, int borderColor) {
        super(area, ScrollDirection.HORIZONTAL, ScrollBar.Visibility.NEVER, borderColor);
        this.setValue(val);
    }


    public SearchBar(Area area, int borderColor) {
        this(area, FormattedString.EMPTY, borderColor);
    }


    public SearchBar(Area area, FormattedString val, int borderColor) {
        this(area, new FormattedCharSeq(val), borderColor);
    }


    @Override
    public void seekCursorToPoint(double x, double y) {
        float widthDif = (float) (x - (this.contentArea.getInnerX() - this.xScrollBar.getScrollAmount()));
        int i = this.value.getIndexAtWidth(widthDif);
        this.changeCursorPosition(Whence.ABSOLUTE, i);
    }


    @Override
    protected void updateCursorDisplayPos() {
        this.cursor.setDisplayPos(this.contentArea.getInnerX() + this.value.subSeq(0, this.cursor.getCharPos()).width() - (this.cursor.getLineWidth() / 2), ALIGNMENT.getYPos(this.contentArea, this.cursor.getHeight() - 2, 0), this.contentArea.getZ());
    }


    @Override
    protected void scrollToCursor() {
        double amount = this.xScrollBar.getScrollAmount();

        float xPos = this.contentArea.getInnerX();
        float contentWidth = this.value.subSeq(0, this.cursor.getCharPos()).width();

        if(xPos + contentWidth - amount >= this.contentArea.getInnerXMax()) {
            amount = xPos + contentWidth - this.contentArea.getInnerXMax();
        }
        else if(this.cursor.getXPos() <= this.contentArea.getInnerX() + amount) {
            amount = this.cursor.getXPos() - this.contentArea.getInnerX();
        }

        this.xScrollBar.setScrollAmount(amount);
    }


    @Override
    protected void onEnter() {
        if(this.enterListener != null) {
            this.enterListener.accept(this.value);
        }
    }


    @Override
    protected StringSelection getCursorLine() {
        return new StringSelection(0, this.value.length());
    }


    @Override
    protected double scrollRate() {
        return 9.0D / 2.0D;
    }


    @Override
    protected void renderContents(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.value.isEmpty() && this.suggestion != null) {
            FontRenderer.draw(poseStack, this.suggestion, this.contentArea.getInnerX(), ALIGNMENT.getYPos(this.contentArea, this.suggestion.getMaxCharHeight() - this.suggestion.getMaxLineSpacing(), 0), this.contentArea.getZ());
        }
        else {
            FontRenderer.draw(poseStack, this.value, this.contentArea.getInnerX(), ALIGNMENT.getYPos(this.contentArea, this.value.getMaxCharHeight() - this.value.getMaxLineSpacing(), 0), this.contentArea.getZ());
        }

        if(this.hasSelection() && this.isEditable()) {
            StringSelection selection = this.getSelected();
            FormattedCharSeq content = selection.getContent();

            float yStart = ALIGNMENT.getYPos(this.contentArea.getInnerY() - 1, this.contentArea.getInnerHeight(), this.value.getMaxCharHeight() - this.value.getMaxLineSpacing(), 0);
            float yStop = yStart + content.getMaxCharHeight() + 2;

            float xStart = this.contentArea.getInnerX() - 1;
            xStart += this.value.subSeq(0, selection.beginIndex).width();
            float xStop = this.contentArea.getInnerX() + this.value.width();
            xStop -= this.value.subSeq(selection.endIndex, this.value.length()).width();

            this.renderHighlight(poseStack, xStart, yStart, xStop, yStop);
        }

        this.renderCursor(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    protected float contentWidth() {
        return this.value.width();
    }


    public void onEnter(Consumer<FormattedCharSeq> enterListener) {
        this.enterListener = enterListener;
    }
}
