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
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.minecraft.SharedConstants.isAllowedChatCharacter;
import static org.lwjgl.glfw.GLFW.*;


//TODO: Add selecting with double mouse click
public abstract class AbstractTextField extends ScrollableScreenComponent {

    protected final Cursor cursor;

    private final int borderColor;

    protected FormattedCharSeq value;

    protected Consumer<FormattedCharSeq> valueListener = s -> {};

    @Nullable
    protected FormattedCharSeq suggestion;

    private boolean selecting;

    private int characterLimit = Integer.MAX_VALUE;

    private boolean editable = false;

    protected BooleanSupplier shouldRenderCursor = this::isEditable;

    protected Runnable cursorListener = () -> {
        if(this.shouldRenderCursor.getAsBoolean() && this.isFocused()) {
            this.scrollToCursor();
        }
    };


    public AbstractTextField(Area area, ScrollDirection scrollDirection, ScrollBar.Visibility scrollbarVisibility) {
        this(area, scrollDirection, scrollbarVisibility, 0);
    }


    public AbstractTextField(Area area, ScrollDirection scrollDirection, ScrollBar.Visibility scrollbarVisibility, int borderColor) {
        super(area, scrollDirection, scrollbarVisibility);
        this.cursor = new Cursor(area.getInnerX(), area.getInnerY(), this.contentArea.getZ(), 0, (charPos) -> {
            if(this.value.isEmpty()) {
                return FontFormatting.defaultFormatting();
            }
            else {
                return this.value.charAt(Mth.clamp(charPos, 0, this.value.length() - 1)).format();
            }
        });
        this.borderColor = borderColor;
    }


    public void setSuggestion(@Nullable Component suggestion) {
        this.suggestion = suggestion != null ? new FormattedCharSeq(new FormattedString(suggestion, FontFormatting.defaultFormatting().setColor(122, 122, 122, 255 * 0.75f))) : null;
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
            return false;
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
    protected void renderDecorations(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.hasCharacterLimit()) {
            int i = this.characterLimit();
            Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.value.length(), i);
            ProgressionFont font = FontManager.getInstance().getFont(new ResourceLocation("default")).setLineHeight(3).setColor(-857677600).setAlpha(this.alpha);
            font.draw(poseStack, component, this.area.getXMax() - (font.width(component) + 2), this.area.getYMax() - (font.getLineHeight() + 2), this.area.getZ() - 1);
        }

        if(this.borderColor != 0) {
            RenderUtil.renderArea(poseStack, this.area, RenderUtil.changeAlpha(borderColor, this.alpha), 0);
        }
    }


    public int characterLimit() {
        return this.characterLimit;
    }


    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }


    public abstract void seekCursorToPoint(double x, double y);


    public void setCharacterLimit(int limit) {
        if(limit < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        }
        else {
            this.characterLimit = limit;
        }

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


    protected void onValueChange() {
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


    protected abstract void updateCursorDisplayPos();


    public void insertChar(FormattedChar formattedChar) {
        this.insertText(new FormattedCharSeq(formattedChar));
    }


    public void insertText(FormattedCharSeq text) {
        if(!text.isEmpty() || this.hasSelection()) {
            FormattedCharSeq s = this.trimInsertionText(filterText(text, true));
            AbstractTextField.StringSelection selected = this.getSelected();

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


    public boolean hasSelection() {
        return this.cursor.hasSelection();
    }


    private FormattedCharSeq trimInsertionText(FormattedCharSeq text) {
        if(this.hasCharacterLimit()) {
            int i = this.characterLimit - this.value.length();
            String s = StringUtil.truncateStringIfNecessary(text.toString(), i, false);
            text = text.subSeq(0, s.length());
        }
        return text;
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


    protected AbstractTextField.StringSelection getSelected() {
        return new AbstractTextField.StringSelection(Math.min(this.cursor.getSelectPos(), this.cursor.getCharPos()), Math.max(this.cursor.getSelectPos(), this.cursor.getCharPos()));
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


    protected void moveCursorPos(int amount) {
        this.cursor.moveCharPos(amount);
        this.setCursorPos(Mth.clamp(this.cursor.getCharPos(), 0, this.value.length()));
    }


    protected abstract void scrollToCursor();


    protected void renderHighlight(PoseStack poseStack, float startX, float startY, float endX, float endY) {
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
        bufferbuilder.vertex(matrix4f, startX, endY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, endY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, endX, startY, this.contentArea.getZ()).endVertex();
        bufferbuilder.vertex(matrix4f, startX, startY, this.contentArea.getZ()).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    protected void renderCursor(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.shouldRenderCursor.getAsBoolean() && this.isFocused() && this.withinContentAreaTopBottom(this.cursor.getYPos() - 2, this.cursor.getYPos() + this.cursor.getFormat().getLineHeight() + 2)) {
            this.updateCursorDisplayPos();
            this.cursor.render(poseStack, mouseX, mouseY, partialTick);
        }
    }


    public FormattedCharSeq getSelectedText() {
        AbstractTextField.StringSelection selected = this.getSelected();
        return selected.getContent();
    }


    protected AbstractTextField.StringSelection getPreviousWord() {
        if(this.value.isEmpty()) {
            return new AbstractTextField.StringSelection(0, 0);
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

            return new AbstractTextField.StringSelection(i, this.getWordEndPosition(i));
        }
    }


    protected AbstractTextField.StringSelection getNextWord() {
        if(this.value.isEmpty()) {
            return new AbstractTextField.StringSelection(0, 0);
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

            return new AbstractTextField.StringSelection(i, this.getWordEndPosition(i));
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
                        this.onEnter();
                        yield true;
                    }
                    case GLFW_KEY_TAB -> {
                        this.onTab();
                        yield true;
                    }
                    case GLFW_KEY_BACKSPACE -> {
                        if(Screen.hasControlDown()) {
                            AbstractTextField.StringSelection previousWord = this.getPreviousWord();
                            this.deleteText(previousWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(-1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_DELETE -> {
                        if(Screen.hasControlDown()) {
                            AbstractTextField.StringSelection nextWord = this.getNextWord();
                            this.deleteText(nextWord.beginIndex - this.cursor.getCharPos());
                        }
                        else {
                            this.deleteText(1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_RIGHT -> {
                        if(Screen.hasControlDown()) {
                            AbstractTextField.StringSelection nextWord = this.getNextWord();
                            this.changeCursorPosition(Whence.ABSOLUTE, nextWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, 1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_LEFT -> {
                        if(Screen.hasControlDown()) {
                            AbstractTextField.StringSelection previousWord = this.getPreviousWord();
                            this.changeCursorPosition(Whence.ABSOLUTE, previousWord.beginIndex);
                        }
                        else {
                            this.changeCursorPosition(Whence.RELATIVE, -1);
                        }
                        yield true;
                    }
                    case GLFW_KEY_DOWN -> {
                        if(!Screen.hasControlDown()) {
                            this.onDown();
                        }
                        yield true;
                    }
                    case GLFW_KEY_UP -> {
                        if(!Screen.hasControlDown()) {
                            this.onUp();
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


    public void insertText(FormattedString text) {
        this.insertText(new FormattedCharSeq(text));
    }


    protected void onTab() {}


    protected void onEnter() {}


    protected abstract StringSelection getCursorLine();


    protected void onUp() {}


    protected void onDown() {}


    @Override
    public ScreenComponent setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.value.setForAll(fontFormatting -> fontFormatting.setAlpha(alpha));
        return this;
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", "", this.value()));
    }


    public FormattedCharSeq value() {
        return this.value;
    }


    public void setEditable() {
        this.editable = true;
    }


    public void setUnEditable() {
        this.editable = false;

    }


    public void clear() {
        this.setValue(FormattedCharSeq.EMPTY.get());

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


    @OnlyIn(Dist.CLIENT)
    protected class StringSelection {

        protected final int beginIndex, endIndex;


        StringSelection(int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }


        public FormattedCharSeq getContent() {
            return AbstractTextField.this.value.subSeq(Math.max(this.beginIndex, 0), Math.min(this.endIndex + 1, AbstractTextField.this.value.length()));
        }


        public boolean contains(int charPos) {
            return charPos >= this.beginIndex && charPos <= this.endIndex;
        }
    }
}
