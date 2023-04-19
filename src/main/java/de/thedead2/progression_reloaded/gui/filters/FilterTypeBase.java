package de.thedead2.progression_reloaded.gui.filters;

import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import net.minecraft.item.ItemStack;

import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;

public abstract class FilterTypeBase implements IFilterType {
    @Override
    public int getChange() {
        return 200;
    }
    
    @Override
    public double getScale() {
        return 16.133333334D;
    }
    
    @Override
    public boolean searchMatches(Object object, String search) {
        ItemStack stack = (ItemStack) object;
        try {
            if (stack.getItem() != null) {
                if (stack.getDisplayName().toLowerCase().contains(search)) {
                    return true;
                }
            }
        } catch (Exception e) {}

        return false;
    }

    @Override
    public void draw(IDrawHelper offset, Object object, int offsetX, int j, int yOffset, int k, int mouseX, int mouseY) {
        ItemStack stack = (ItemStack) object;
        offset.drawStack(stack, -offsetX + 8 + (j * 16), yOffset + 45 + (k * 16), 1F);
        if (mouseX >= 8 + (j * 16) && mouseX < 8 + (j * 16) + 16) {
            if (mouseY >= 45 + (k * 16) && mouseY < 45 + (k * 16) + 16) {
                TOOLTIP.add(stack.getTooltip(MCClientHelper.getPlayer(), false));
            }
        }

    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int j, int k) {
        if (mouseX >= 8 + (j * 16) && mouseX <= 8 + (j * 16) + 16) {
            if (mouseY >= 45 + (k * 16) && mouseY <= 45 + (k * 16) + 16) {
                return true;
            }
        }

        return false;
    }
}
