package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.handlers.TemplateHandler;
import de.thedead2.progression_reloaded.helpers.RenderItemHelper;
import de.thedead2.progression_reloaded.json.DataTab;
import de.thedead2.progression_reloaded.json.JSONLoader;

import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class FeatureTemplateSelectorTab extends FeatureTemplateAbstract {
    @Override
    public boolean clickForeground(int mouseX, int mouseY, int width) {
        int j = 0, k = 0;
        for (int i = 0; i < TemplateHandler.getTabs().size(); i++) {
            if (isMouseOver(mouseX, mouseY, j, k)) {
                DataTab tab = TemplateHandler.getTabs().get(i);
                if (APICache.getClientCache().getTab(tab.getUUID()) == null) {
                    JSONLoader.createTabFromData(tab, true);
                    JSONLoader.initEverything(true);
                    TREE_EDITOR.onClientSetup();
                    CORE.setEditor(TREE_EDITOR);
                }

                return false;
            }

            //Update stuff
            j++;
            if (j > width) {
                j = 0;
                k++;
            }
        }

        return false;
    }


    @Override
    protected void drawForeground(int mouseX, int mouseY, int width) {
        int j = 0;
        int k = 0;
        for (int i = 0; i < TemplateHandler.getTabs().size(); i++) {
            DataTab tab = TemplateHandler.getTabs().get(i);
            RenderItemHelper.drawStack(tab.getIcon(), 32 + (j * 16), CORE.screenTop + 45 + (k * 16), 1F);
            if (isMouseOver(mouseX, mouseY, j, k)) {
                TOOLTIP.add(tab.getName());
            }

            //Update stuff
            j++;
            if (j > width) {
                j = 0;
                k++;
            }
        }
    }
}