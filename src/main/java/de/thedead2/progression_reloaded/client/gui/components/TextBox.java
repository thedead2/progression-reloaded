package de.thedead2.progression_reloaded.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.components.misc.Cursor;
import de.thedead2.progression_reloaded.client.gui.components.misc.ScrollBar;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedChar;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.fonts.rendering.FontRenderer;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.minecraft.SharedConstants.isAllowedChatCharacter;
import static org.lwjgl.glfw.GLFW.*;


public class TextBox extends ScrollableScreenComponent {

    protected final List<DisplayLine> displayLines = Lists.newArrayList();

    protected final Cursor cursor;

    private final int borderColor;

    private final MenuBar menuBar;

    protected FormattedCharSeq value;

    private boolean selecting;

    private int characterLimit = Integer.MAX_VALUE;

    private Consumer<FormattedCharSeq> valueListener = s -> {};

    private boolean editable = false;

    private BooleanSupplier shouldRenderCursor = this::isEditable;

    private Runnable cursorListener = () -> {
        if(this.shouldRenderCursor.getAsBoolean()) {
            this.scrollToCursor();
        }
    };


    public TextBox(Area area) {
        this(area, FormattedCharSeq.EMPTY.get(), 0);
    }


    public TextBox(Area area, FormattedCharSeq val, int borderColor) {
        super(area, ScrollDirection.VERTICAL, ScrollBar.Visibility.IF_NECESSARY);
        this.cursor = new Cursor(area.getInnerX(), area.getInnerY(), this.area.getZ(), 0, (charPos) -> {
            if(this.value.isEmpty()) {
                return FontFormatting.defaultFormatting();
            }
            else {
                return this.value.charAt(Mth.clamp(charPos, 0, this.value.length() - 1)).format();
            }
        });
        this.menuBar = new MenuBar(this.area.copy().setPadding(2).setHeight(10));
        this.borderColor = borderColor;
        this.setValue(val);
    }


    public void setValue(FormattedCharSeq text) {
        this.value = this.trimTextIfNecessary(text);
        this.onValueChange();
        this.setCursorPos(this.value.length());
        this.cursor.setSelectToCurrentPos();
    }


    private FormattedCharSeq trimTextIfNecessary(FormattedCharSeq text) {
        if(this.hasCharacterLimit()) {
            String s = StringUtil.truncateStringIfNecessary(text.toString(), this.characterLimit, false);
            text = text.subSeq(0, s.length());
        }
        return text;
    }


    private void onValueChange() {
        this.recalculateDisplayLines();
        this.valueListener.accept(this.value);
    }


    protected void setCursorPos(int charPos) {
        this.cursor.setCharPos(Mth.clamp(charPos, 0, this.value.length()));
        this.updateCursorDisplayPos();
        this.cursorListener.run();
    }


    public boolean hasCharacterLimit() {
        return this.characterLimit != Integer.MAX_VALUE;
    }


    private void recalculateDisplayLines() {
        this.displayLines.clear();
        if(this.value.isEmpty()) {
            this.displayLines.add(new DisplayLine(0, 0, Alignment.XAlignment.LEFT));
        }
        else {
            this.value.splitLines(this.area.getInnerWidth(), (i, j) -> {
                this.displayLines.add(new DisplayLine(i, j, this.cursor.getFormat().getTextAlignment().xAlignment()));
            });
            if(this.value.charAt(this.value.length() - 1).character() == '\n') {
                this.displayLines.add(new DisplayLine(this.value.length(), this.value.length(), this.cursor.getFormat().getTextAlignment().xAlignment()));
            }
        }
    }


    protected void updateCursorDisplayPos() {
        DisplayLine line = this.getCursorLine();
        this.cursor.setDisplayPos(line.textAlignment.getXPos(this.area.getInnerX(), this.area.getInnerWidth(), line.getContent().width()) + this.value.subSeq(line.beginIndex, this.cursor.getCharPos())
                                                                                                                                                      .width() - (this.cursor.getLineWidth() / 2), this.getYPosByLineIndex(this.displayLines.indexOf(line)), this.area.getZ());
    }


    private DisplayLine getCursorLine() {
        return this.getLineWithOffsetFromCursorLine(0);
    }


    protected float getYPosByLineIndex(int index) {
        float yStart = this.area.getInnerY();

        for(int i = 0; i < Mth.clamp(index, 0, this.displayLines.size()); i++) {
            DisplayLine line = this.displayLines.get(i);
            FormattedCharSeq lineContent = line.getContent();
            float lineHeight = lineContent.getMaxCharHeight();
            yStart += lineHeight;
        }

        return yStart;
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


    public TextBox(Area area, int borderColor) {
        this(area, FormattedCharSeq.EMPTY.get(), borderColor);
    }


    public TextBox(Area area, List<FormattedString> strings) {
        this(area, new FormattedCharSeq(strings), 0);
    }


    public TextBox(Area area, List<FormattedString> strings, int borderColor) {
        this(area, new FormattedCharSeq(strings), borderColor);
    }


    public TextBox(Area area, FormattedCharSeq val) {
        this(area, val, 0);
    }


    public TextBox(Area area, FormattedString val) {
        this(area, val, 0);
    }


    public TextBox(Area area, FormattedString val, int borderColor) {
        this(area, new FormattedCharSeq(val), borderColor);
    }


    public static FormattedCharSeq filterText(FormattedCharSeq text, boolean allowLineBreaks) {
        List<FormattedChar> chars = Lists.newArrayListWithCapacity(text.length());
        for(FormattedChar formattedChar : text.getChars()) {
            if(isAllowedChatCharacter(formattedChar.character())) {
                chars.add(formattedChar);
            }
            else if(allowLineBreaks && formattedChar.character() == '\n') {
                chars.add(formattedChar);
            }
        }
        return new FormattedCharSeq(chars);
    }


    @Override
    protected double scrollRate() {
        return 9.0D / 2.0D;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        else if(this.withinContentArea(mouseX, mouseY) && button == 0) {
            this.setSelecting(Screen.hasShiftDown());
            this.seekCursorToPoint(mouseX, mouseY);
            return true;
        }
        else {
            return this.menuBar.mouseClicked(mouseX, mouseY, button);
        }
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        else if(this.withinContentArea(mouseX, mouseY) && button == 0) {
            this.setSelecting(true);
            this.seekCursorToPoint(mouseX, mouseY);
            this.setSelecting(Screen.hasShiftDown());
            return true;
        }
        else {
            return false;
        }
    }


    @Override
    public void renderContents(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for(int i = 0; i < this.displayLines.size(); i++) {
            float yPos = this.getYPosByLineIndex(i);
            DisplayLine line = this.displayLines.get(i);
            FormattedCharSeq lineContent = line.getContent();
            if(!this.withinContentAreaTopBottom(yPos, yPos + lineContent.getMaxCharHeight())) {
                continue;
            }

            float xPos = line.textAlignment.getXPos(this.area.getInnerX(), this.area.getInnerWidth(), lineContent.width());

            FontRenderer.draw(poseStack, lineContent, xPos, yPos, this.area.getZ());
        }

        if(this.hasSelection() && this.editable) {
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

                float xStart = line.textAlignment.getXPos(this.area.getInnerX(), this.area.getInnerWidth(), lineWidth) - 1;
                if(line.contains(selection.beginIndex)) {
                    xStart += this.value.subSeq(line.beginIndex, selection.beginIndex).width();
                }
                float xStop = line.textAlignment.getXPos(this.area.getInnerX(), this.area.getInnerWidth(), lineWidth) + lineWidth;
                if(line.contains(selection.endIndex)) {
                    xStop -= this.value.subSeq(selection.endIndex, line.endIndex).width();
                }

                this.renderHighlight(poseStack, xStart, yStart, xStop, yStop);
            }
        }

        if(this.shouldRenderCursor.getAsBoolean() && this.withinContentAreaTopBottom(this.cursor.getYPos() - 2, this.cursor.getYPos() + this.cursor.getFormat().getLineHeight() + 2)) {
            this.updateCursorDisplayPos();
            this.cursor.render(poseStack);
        }
    }


    public boolean hasSelection() {
        return this.cursor.hasSelection();
    }


    protected StringSelection getSelected() {
        return new StringSelection(Math.min(this.cursor.getSelectPos(), this.cursor.getCharPos()), Math.max(this.cursor.getSelectPos(), this.cursor.getCharPos()));
    }


    private void renderHighlight(PoseStack poseStack, float startX, float startY, float endX, float endY) {
        Matrix4f matrix4f = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor((float) 0, (float) 102 / 255, 1, 0.5f);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(matrix4f, startX, endY, this.area.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, endY, this.area.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, startY, this.area.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, startX, startY, this.area.getZ()).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    @Override
    protected void renderDecorations(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.editable) {
            this.menuBar.render(poseStack, mouseX, mouseY, partialTick);
        }

        if(this.hasCharacterLimit()) {
            int i = this.characterLimit();
            Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.value.length(), i);
            ProgressionFont font = FontManager.getFont(new ResourceLocation("default")).setLineHeight(3).setColor(-857677600).setAlpha(this.alpha);
            font.draw(poseStack, component, this.area.getXMax() - (font.width(component) + 2), this.area.getYMax() - (font.getLineHeight() + 2), this.area.getZ() - 1);
        }

        if(this.borderColor != 0) {
            RenderUtil.renderArea(poseStack, this.area, RenderUtil.changeAlpha(borderColor, this.alpha), 0);
        }
    }


    public int characterLimit() {
        return this.characterLimit;
    }


    protected float contentHeight() {
        float height = 0;
        for(DisplayLine line : this.displayLines) {
            FormattedCharSeq lineContent = line.getContent();
            height += lineContent.getMaxCharHeight();
        }
        return height;
    }


    @Override
    protected float contentWidth() {
        return this.area.getInnerWidth();
    }


    public TextBox setCharacterLimit(int limit) {
        if(limit < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        }
        else {
            this.characterLimit = limit;
        }
        return this;
    }


    public void setValueListener(Consumer<FormattedCharSeq> listener) {
        this.valueListener = listener;
    }


    public void setCursorListener(Runnable listener) {
        this.cursorListener = listener;
    }


    public void setValue(FormattedString text) {
        this.setValue(new FormattedCharSeq(text));
    }


    public void insertChar(FormattedChar formattedChar) {
        this.insertText(new FormattedCharSeq(formattedChar));
    }


    public void insertText(FormattedString text) {
        this.insertText(new FormattedCharSeq(text));
    }


    public void insertText(FormattedCharSeq text) {
        if(!text.isEmpty() || this.hasSelection()) {
            FormattedCharSeq s = this.trimInsertionText(filterText(text, true));
            StringSelection selected = this.getSelected();

            if(text.isEmpty()) {
                this.setCursorPos(selected.beginIndex);
                this.value.delete(selected.beginIndex, selected.endIndex);
                this.onValueChange();
                //Update the position again as the line positions might have changed!
                this.updateCursorDisplayPos();
            }
            else {
                if(this.hasSelection()) {
                    this.value.replace(selected.beginIndex, selected.endIndex, s);
                }
                else {
                    this.value.insert(selected.beginIndex, s);
                }
                this.onValueChange();
                this.setCursorPos(selected.beginIndex + s.length());
            }
            this.cursor.setSelectToCurrentPos();
        }
    }


    public void deleteText(int len) {
        if(!this.hasSelection()) {
            this.cursor.setSelectPos(Mth.clamp(this.cursor.getCharPos() + len, 0, this.value.length()));
        }

        this.insertText(FormattedString.EMPTY);
    }


    public Cursor cursor() {
        return this.cursor;
    }


    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
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


    public void changeCursorPosition(Whence whence, int amount) {
        switch(whence) {
            case ABSOLUTE:
                this.setCursorPos(amount);
                break;
            case RELATIVE:
                this.moveCursorPos(amount);
                break;
            case END:
                this.setCursorPos(this.value.length());
        }

        if(!this.selecting) {
            this.cursor.setSelectToCurrentPos();
        }
    }


    public void changeCursorLinePos(int offset) {
        if(offset != 0) {
            DisplayLine lineAtOffset = this.getLineWithOffsetFromCursorLine(offset);
            DisplayLine currentCursorLine = this.getCursorLine();

            int dif = this.value.subSeq(currentCursorLine.beginIndex, this.cursor.getCharPos()).length();
            this.changeCursorPosition(Whence.ABSOLUTE, lineAtOffset.beginIndex + Math.min(dif, lineAtOffset.endIndex - lineAtOffset.beginIndex));
        }
    }


    public void seekCursorToPoint(double x, double y) {
        int j = this.getLineIndexByYPos(y);
        DisplayLine line = this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
        float widthDif = (float) (x - line.textAlignment.getXPos(this.area.getInnerX(), this.area.getInnerWidth(), line.getContent().width()));
        FormattedCharSeq lineContent = line.getContent();
        int i = lineContent.getIndexAtWidth(widthDif);
        this.changeCursorPosition(Whence.ABSOLUTE, line.beginIndex + i);
    }


    protected int getLineIndexByYPos(double y) {
        float yStart = (float) (this.area.getInnerY() - this.yScrollBar.getScrollAmount());

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


    protected void moveCursorPos(int amount) {
        this.cursor.moveCharPos(amount);
        this.setCursorPos(Mth.clamp(this.cursor.getCharPos(), 0, this.value.length()));
    }


    private void scrollToCursor() {
        double amount = this.yScrollBar.getScrollAmount();

        int i = this.getCursorLineIndex();
        float contentHeight = this.getCursorLine().getContent().getMaxCharHeight();
        float yPos = this.getYPosByLineIndex(i);
        if(yPos + contentHeight - amount >= this.area.getInnerYMax()) {
            amount = yPos + contentHeight - this.area.getInnerYMax();
        }
        else if(yPos <= this.area.getInnerY() + amount) {
            amount = yPos - this.area.getInnerY();
        }

        this.yScrollBar.setScrollAmount(amount);
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.selecting = Screen.hasShiftDown();
        if(Screen.isSelectAll(keyCode)) {
            this.setCursorPos(this.value.length());
            this.cursor.setSelectPos(0);
            return true;
        }
        else if(Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText().toString());
            return true;
        }
        else if(Screen.isPaste(keyCode)) {
            this.insertText(new FormattedString(Minecraft.getInstance().keyboardHandler.getClipboard(), this.cursor.getFormat()));
            return true;
        }
        else if(Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText().toString());
            this.insertText(FormattedString.EMPTY);
            return true;
        }
        else if(this.editable) {
            //TODO: Add Undo/ Redo functionality!
            if(ModClientInstance.isUndo(keyCode)) {
                ModHelper.LOGGER.debug("Undo requested!");
                return true;
            }
            else if(ModClientInstance.isRedo(keyCode)) {
                ModHelper.LOGGER.debug("Redo requested!");
                return true;
            }
            else {
                return switch(keyCode) {
                    case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER -> {
                        this.insertText(new FormattedString("\n", this.cursor.getFormat()));
                        yield true;
                    }
                    case GLFW_KEY_BACKSPACE -> {
                        if(Screen.hasControlDown()) {
                            StringSelection previousWord = this.getPreviousWord();
                            this.deleteText(previousWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(-1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_DELETE -> {
                        if(Screen.hasControlDown()) {
                            StringSelection nextWord = this.getNextWord();
                            this.deleteText(nextWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_RIGHT -> {
                        if(Screen.hasControlDown()) {
                            StringSelection nextWord = this.getNextWord();
                            this.changeCursorPosition(Whence.ABSOLUTE, nextWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, 1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_LEFT -> {
                        if(Screen.hasControlDown()) {
                            StringSelection previousWord = this.getPreviousWord();
                            this.changeCursorPosition(Whence.ABSOLUTE, previousWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, -1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_DOWN -> {
                        if(!Screen.hasControlDown()) {
                            this.changeCursorLinePos(1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_UP -> {
                        if(!Screen.hasControlDown()) {
                            this.changeCursorLinePos(-1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_PAGE_UP -> {
                        this.changeCursorPosition(Whence.ABSOLUTE, 0);
                        yield true;
                    }
                    case GLFW_KEY_PAGE_DOWN -> {
                        this.changeCursorPosition(Whence.END, 0);
                        yield true;
                    }
                    case GLFW_KEY_HOME -> {
                        if(Screen.hasControlDown()) {
                            this.changeCursorPosition(Whence.ABSOLUTE, 0);
                        }
                        else {
                            this.changeCursorPosition(Whence.ABSOLUTE, this.getCursorLine().beginIndex);
                        }
                        yield true;
                    }
                    case GLFW_KEY_END -> {
                        if(Screen.hasControlDown()) {
                            this.changeCursorPosition(Whence.END, 0);
                        }
                        else {
                            this.changeCursorPosition(Whence.ABSOLUTE, this.getCursorLine().endIndex);
                        }
                        yield true;
                    }
                    default -> false;
                };
            }
        }
        else {
            return false;
        }
    }


    public boolean charTyped(char codePoint, int modifiers) {
        if(this.editable && SharedConstants.isAllowedChatCharacter(codePoint)) {
            this.insertText(new FormattedString(Character.toString(codePoint), this.cursor.getFormat()));
            return true;
        }
        else {
            return false;
        }
    }


    protected Iterable<DisplayLine> iterateLines() {
        return this.displayLines;
    }


    public FormattedCharSeq getSelectedText() {
        StringSelection selected = this.getSelected();
        return selected.getContent();
    }


    protected StringSelection getPreviousWord() {
        if(this.value.isEmpty()) {
            return new StringSelection(0, 0);
        }
        else {
            int i;
            for(i = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); i > 0; --i) {
                if(!Character.isWhitespace(this.value.charAt(i - 1).character())) {
                    break;
                }
            }

            while(i > 0 && !Character.isWhitespace(this.value.charAt(i - 1).character())) {
                --i;
            }

            return new StringSelection(i, this.getWordEndPosition(i));
        }
    }


    protected StringSelection getNextWord() {
        if(this.value.isEmpty()) {
            return new StringSelection(0, 0);
        }
        else {
            int i;
            for(i = Mth.clamp(this.cursor.getCharPos(), 0, this.value.length() - 1); i < this.value.length(); ++i) {
                if(Character.isWhitespace(this.value.charAt(i).character())) {
                    break;
                }
            }

            while(i < this.value.length() && Character.isWhitespace(this.value.charAt(i).character())) {
                ++i;
            }

            return new StringSelection(i, this.getWordEndPosition(i));
        }
    }


    private int getWordEndPosition(int cursorPos) {
        int i;
        for(i = cursorPos; i < this.value.length(); ++i) {
            if(Character.isWhitespace(this.value.charAt(i).character())) {
                break;
            }
        }

        return i;
    }


    private FormattedCharSeq trimInsertionText(FormattedCharSeq text) {
        if(this.hasCharacterLimit()) {
            int i = this.characterLimit - this.value.length();
            String s = StringUtil.truncateStringIfNecessary(text.toString(), i, false);
            text = text.subSeq(0, s.length());
        }
        return text;
    }


    @Override
    public ScreenComponent setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.value.setAlphaForAll(alpha);
        return this;
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", "", this.value()));
    }


    public FormattedCharSeq value() {
        return this.value;
    }


    public TextBox setEditable() {
        this.editable = true;
        return this;
    }


    public TextBox setUnEditable() {
        this.editable = false;
        return this;
    }


    public TextBox clear() {
        this.setValue(FormattedCharSeq.EMPTY.get());
        return this;
    }


    public void setRenderCursor(BooleanSupplier renderCursor) {
        this.shouldRenderCursor = renderCursor;
    }


    public void resetRenderCursor() {
        this.shouldRenderCursor = this::isEditable;
    }


    public boolean isEditable() {
        return this.editable;
    }


    public static class MenuBar extends ScreenComponent {

        protected MenuBar(Area area) {
            super(area);
        }


        @Override
        public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

        }
    }

    protected class DisplayLine extends StringSelection {

        private final Alignment.XAlignment textAlignment;


        DisplayLine(int beginIndex, int endIndex, Alignment.XAlignment textAlignment) {
            super(beginIndex, endIndex);
            this.textAlignment = textAlignment;
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected class StringSelection {

        protected final int beginIndex, endIndex;


        StringSelection(int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }


        public FormattedCharSeq getContent() {
            return TextBox.this.value.subSeq(Math.max(this.beginIndex, 0), Math.min(this.endIndex + 1, TextBox.this.value.length()));
        }


        public boolean contains(int charPos) {
            return charPos >= this.beginIndex && charPos <= this.endIndex;
        }
    }
}
