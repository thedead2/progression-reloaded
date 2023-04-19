package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.api.criteria.ITab;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.handlers.TemplateHandler;
import de.thedead2.progression_reloaded.helpers.RenderItemHelper;
import de.thedead2.progression_reloaded.json.DataCriteria;
import de.thedead2.progression_reloaded.json.JSONLoader;

import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class FeatureTemplateSelectorCriteria extends FeatureTemplateAbstract {
    @Override
    public boolean clickForeground(int mouseX, int mouseY, int width) {
        int j = 0, k = 0;
        for (int i = 0; i < TemplateHandler.getCriteria().size(); i++) {
            if (isMouseOver(mouseX, mouseY, j, k)) {
                DataCriteria criteria = TemplateHandler.getCriteria().get(i);
                if (APICache.getClientCache().getCriteria(criteria.getUUID()) == null) {
                    ITab tab = TREE_EDITOR.currentTab;
                    if (tab != null) {
                        JSONLoader.createCriteriaFromData(tab, criteria, true);
                        JSONLoader.createCriteriaInternals(criteria, true);
                        JSONLoader.initialiseCriteria(criteria, true);
                        JSONLoader.initEverything(true);
                        CORE.setEditor(TREE_EDITOR);
                    }
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
        for (int i = 0; i < TemplateHandler.getCriteria().size(); i++) {
            DataCriteria criteria = TemplateHandler.getCriteria().get(i);
            RenderItemHelper.drawStack(criteria.getIcon(), 32 + (j * 16), CORE.screenTop + 45 + (k * 16), 1F);
            if (isMouseOver(mouseX, mouseY, j, k)) {
                TOOLTIP.add(criteria.getName());
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