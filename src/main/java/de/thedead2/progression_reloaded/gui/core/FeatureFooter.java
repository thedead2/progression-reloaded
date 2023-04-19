package de.thedead2.progression_reloaded.gui.core;

import static de.thedead2.progression_reloaded.gui.core.GuiList.THEME;

public class FeatureFooter extends FeatureAbstract {
    @Override
    public void drawFeature(int mouseX, int mouseY) {
        offset.drawRectangle(-1, 215, screenWidth, 1, THEME.blackBarUnderLineBorder, THEME.blackBarUnderLineBorder);
        offset.drawText(de.thedead2.progression_reloaded.ProgressionReloaded.translate("footer.line1"), 9, 218, THEME.scrollTextFontColor);
        offset.drawText(de.thedead2.progression_reloaded.ProgressionReloaded.translate("footer.line2"), 9, 228, THEME.scrollTextFontColor);
    }

    @Override
    public boolean isOverlay() {
        return false;
    }
}
