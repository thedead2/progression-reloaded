package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.api.criteria.IConditionProvider;
import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.gui.editors.insert.FeatureNew;

import java.util.List;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.DISPLAY;
import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class FeatureTrigger extends FeatureDrawable<ITriggerProvider> {
    public FeatureTrigger() {
        super("trigger", 45, NEW_TRIGGER, THEME.triggerGradient1, THEME.triggerGradient2, THEME.triggerFontColor, THEME.triggerBoxGradient2);
    }

    @Override
    public boolean isReady() {
        return CRITERIA_EDITOR.get() != null;
    }

    @Override
    public List<ITriggerProvider> getList() {
        return CRITERIA_EDITOR.get().getTriggers();
    }

    @Override
    public int drawSpecial(ITriggerProvider drawing, int offsetX, int offsetY, int mouseOffsetX, int mouseOffsetY) {
        //We have drawn the deleted button now we check for conditions for triggers.
        if (MODE == EDIT) {
            int color = THEME.blackBarBorder;
            if (mouseOffsetX >= 2 && mouseOffsetX <= drawing.getWidth(MODE) - 3 && mouseOffsetY >= 65 && mouseOffsetY <= 77 && !FeatureNew.IS_OPEN) {
                color = THEME.blackBarFontColor;
            }

            offset.drawGradient(offsetX, offsetY, 2, 65, drawing.getWidth(MODE) - 5, 11, color, THEME.blackBarGradient1, THEME.blackBarGradient2);
            offset.drawText(offsetX, offsetY, de.thedead2.progression_reloaded.ProgressionReloaded.translate((MODE == EDIT ? "editor" : "display") + ".condition"), (drawing.getWidth(EDIT) / 5), 67, THEME.blackBarFontColor);
            offsetX = super.drawSpecial(drawing, offsetX, offsetY, mouseOffsetX, mouseOffsetY);
        } else {
            if (drawing.isVisible()) offsetX = super.drawSpecial(drawing, offsetX, offsetY, mouseOffsetX, mouseOffsetY);
            for (IConditionProvider condition: drawing.getConditions()) {
                if (!condition.isVisible()) continue;
                mouseOffsetX = mouseOffsetX - offsetX;
                drawingDraw(condition, offset, offsetX, offsetY, mouseOffsetX, mouseOffsetY);
                offsetX = offsetX + condition.getWidth(MODE);
            }
        }

        return offsetX;
    }

    @Override
    public boolean clickSpecial(ITriggerProvider provider, int mouseOffsetX, int mouseOffsetY) {
        if (MODE == DISPLAY) return false;
        if (mouseOffsetX >= 2 && mouseOffsetX <= provider.getWidth(MODE) - 3 && mouseOffsetY >= 66 && mouseOffsetY <= 77) {
            CONDITION_EDITOR.set(provider);
            CORE.setEditor(CONDITION_EDITOR);
            return true;
        }

        return false;
    }
}
