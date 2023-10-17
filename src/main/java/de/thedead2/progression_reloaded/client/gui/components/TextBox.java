package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedText;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class TextBox extends ScreenComponent {
    private final List<FormattedText> formattedTexts;
    @Nullable
    private final Integer borderColor;


    public TextBox(Area textArea, List<FormattedText> formattedTexts) {
        this(textArea, formattedTexts, null);
    }
    public TextBox(Area textArea, List<FormattedText> formattedTexts, @Nullable Integer borderColor) {
        super(textArea);
        this.formattedTexts = formattedTexts;
        this.borderColor = borderColor;
    }


    @Override
    public ScreenComponent setAlpha(float alpha) {
        for(FormattedText formattedText : this.formattedTexts) {
            formattedText.formatting().setAlpha(alpha);
        }
        return this;
    }

    //FIXME: When line wrapping keep alignment also for new lines
    //FIXME: When shrinking the area text gets rendered outside the area too
    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        float nextYOffset = 0;
        float completeTextHeight = 0;
        for (FormattedText formattedText : this.formattedTexts) {
            completeTextHeight += formattedText.height(this.area.getInnerWidth());
        }

        for (FormattedText currentText : this.formattedTexts) {
            ProgressionFont currentFont = FontManager.getFont(currentText.font());
            FontFormatting currentFormatting = currentText.formatting();
            Alignment currentTextAlignment = currentFormatting.getTextAlignment();
            currentFont.format(currentFormatting);
            float currentHeight = currentText.height(this.area.getInnerWidth());
            float currentYPos = currentTextAlignment.getYPos(this.area.getInnerY(), this.area.getInnerHeight(), completeTextHeight, nextYOffset);

            nextYOffset += currentHeight;

            if(currentYPos + currentHeight > this.area.getInnerYMax() && !ModRenderer.isGuiDebug())
                continue;
            if (currentText.withShadow()) {
                currentFont.drawShadowWithLineWrap(poseStack, currentText.text(), formattedText -> currentFormatting.getTextAlignment().getXPos(this.area.getInnerX(), this.area.getInnerWidth(), formattedText.width(), 0), currentYPos, this.area.getZ(), this.area.getInnerWidth());
            }
            else {
                currentFont.drawWithLineWrap(poseStack, currentText.text(), formattedText -> currentFormatting.getTextAlignment().getXPos(this.area.getInnerX(), this.area.getInnerWidth(), formattedText.width(), 0), currentYPos, this.area.getZ(), this.area.getInnerWidth());
            }
        }
        if(borderColor != null) {
            RenderUtil.renderArea(poseStack, this.area, borderColor, 0);
        }
    }


    public List<FormattedText> getFormattedTexts() {
        return formattedTexts;
    }
}
