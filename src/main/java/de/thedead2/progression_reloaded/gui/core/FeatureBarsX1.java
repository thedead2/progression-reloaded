package de.thedead2.progression_reloaded.gui.core;

import static de.thedead2.progression_reloaded.gui.core.IBarProvider.BarColorType.*;

public class FeatureBarsX1 extends FeatureAbstract {
    protected IBarProvider provider;
    protected String bar1;

    public FeatureBarsX1(String bar1) {
        this.bar1 = bar1;
    }

    public FeatureBarsX1 setProvider(IBarProvider provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public void drawFeature(int mouseX, int mouseY) {
        offset.drawGradient(-1, 25, screenWidth, 15, provider.getColorForBar(BAR1_GRADIENT1), provider.getColorForBar(BAR1_GRADIENT2), provider.getColorForBar(BAR1_BORDER));
        offset.drawText(de.thedead2.progression_reloaded.ProgressionReloaded.translate(bar1), 9, 29, provider.getColorForBar(BAR1_FONT)); //Removing the offsetX in order to reposition everything back at 0
    }

    @Override
    public boolean isOverlay() {
        return false;
    }
}
