package de.thedead2.progression_reloaded.gui.core;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CORE;

public class FeatureTooltip extends FeatureAbstract {
    private Set<String> tooltip = new LinkedHashSet();

    public FeatureTooltip() {}
    
    @Override
    public FeatureAbstract init() {
        clear();
        return this;
    }
    
    public void clear() {
        tooltip.clear();
    }

    public void add(String tooltip) {
        this.tooltip.add(tooltip);  
    }

    public void add(List<String> list) {
        this.tooltip.addAll(list);
    }

    public void add(String[] split) {
        for (String s: split) {
            this.tooltip.add(s.replace("\r", ""));
        }
    }
    
    @Override
    public void drawFeature(int x, int y) {
        if (!tooltip.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int k = 0;
            Iterator<String> iterator = tooltip.iterator();

            while (iterator.hasNext()) {
                String s = iterator.next();
                int l = CORE.mc.fontRendererObj.getStringWidth(s);

                if (l > k) {
                    k = l;
                }
            }

            int j2 = x + 12;
            int k2 = y - 12;
            int i1 = 8;

            if (tooltip.size() > 1) {
                i1 += 2 + (tooltip.size() - 1) * 10;
            }

            if (j2 + k > CORE.width) {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > CORE.height) {
                k2 = CORE.height - i1 - 6;
            }

            CORE.setZLevel(300.0F);
            int j1 = -267386864;
            CORE.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            CORE.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            CORE.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            CORE.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            CORE.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = GuiList.THEME.toolTipWhite;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            CORE.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            CORE.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            CORE.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            CORE.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            int i2 = 0;
            for (String s1: tooltip) {
                CORE.mc.fontRendererObj.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0) {
                    k2 += 2;
                }

                k2 += 10;
                ++i2;
            }

            CORE.setZLevel(0.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    @Override
    public boolean isOverlay() {
        return true;
    }
}