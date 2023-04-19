package de.thedead2.progression_reloaded.gui.filters;

import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.crafting.ActionType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;

public class FilterTypeAction extends FilterTypeItem {
    public static final IFilterType INSTANCE = new FilterTypeAction();

    @Override
    public String getName() {
        return "crafting";
    }

    @Override
    public List<ItemStack> getAllItems() {
        List<ItemStack> list = new ArrayList();
        for (ActionType type: ActionType.values()) {
            list.add(type.getIcon());
        }
        
        return list;
    }

    @Override
    public boolean isAcceptedItem(ItemStack stack) {
        for (ActionType type: ActionType.values()) {
            if (type.getIcon().getItem() == stack.getItem() && type.getIcon().getItemDamage() == stack.getItemDamage()) return true;
        }
        
        return false;
    }

    @Override
    public void draw(IDrawHelper offset, Object object, int offsetX, int j, int yOffset, int k, int mouseX, int mouseY) {
        ItemStack stack = (ItemStack) object;
        offset.drawStack(stack, -offsetX + 8 + (j * 16), yOffset + 45 + (k * 16), 1F);
        if (mouseX >= 8 + (j * 16) && mouseX < 8 + (j * 16) + 16) {
            if (mouseY >= 45 + (k * 16) && mouseY < 45 + (k * 16) + 16) {
                TOOLTIP.add(ActionType.getCraftingActionFromIcon(stack).getDisplayName());
            }
        }

    }
}
