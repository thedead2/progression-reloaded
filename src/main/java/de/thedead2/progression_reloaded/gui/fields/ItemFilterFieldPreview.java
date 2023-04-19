package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IRuleProvider;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.api.special.IAdditionalTooltip;
import de.thedead2.progression_reloaded.api.special.IClickable;
import de.thedead2.progression_reloaded.api.special.IStackSizeable;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.DISPLAY;
import static de.thedead2.progression_reloaded.gui.core.GuiList.MODE;
import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;

public class ItemFilterFieldPreview extends ItemFilterField implements IField {
    private final int x;
    private final int y;
    private final float scale;
    protected final int mouseX1;
    protected final int mouseX2;
    protected final int mouseY1;
    protected final int mouseY2;
    private ItemStack stack;
    private int ticker;

    public ItemFilterFieldPreview(String fieldName, Object object, int x, int y, float scale) {
        super(fieldName, object);
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.mouseX1 = x;
        this.mouseX2 = (int) (x + 14 * scale);
        this.mouseY1 = y - 2;
        this.mouseY2 = (int) (y + 15 * scale);
    }

    private static final ItemStack BROKEN = new ItemStack(Items.BAKED_POTATO);

    public ItemStack getStack(boolean hovered) {
        if (ticker >= 200 || ticker == 0) {
            stack = ItemHelper.getRandomItemFromFilters(getFilters(), MCClientHelper.getPlayer());
            if (stack != null) {
                stack = stack.copy();
                if (object instanceof IStackSizeable) {
                    stack.stackSize = ((IStackSizeable) object).getStackSize();
                } else stack.stackSize = 1;
            }

            ticker = 1;
        }

        if (!hovered) ticker++;
        else if (!GuiScreen.isShiftKeyDown()) ticker += 2;

        return stack != null ? stack : BROKEN;
    }

    @Override
    public void draw(IRuleProvider provider, IDrawHelper helper, int renderX, int renderY, int color, int yPos, int mouseX, int mouseY) {
        try {
            boolean hovered = mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2;

            if (hovered) {
                List<String> tooltip = new ArrayList();
                ItemStack stack = getStack(hovered);
                tooltip.addAll(stack.getTooltip(MCClientHelper.getPlayer(), false));
                if (object instanceof IAdditionalTooltip) {
                    ((IAdditionalTooltip)object).addHoverTooltip(getFieldName(), stack, tooltip);
                }

                TOOLTIP.add(tooltip);
            }

            helper.drawStack(renderX, renderY, getStack(hovered), x, y, scale);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean attemptClick(int mouseX, int mouseY) {
        boolean clicked = mouseX >= mouseX1 && mouseX <= mouseX2 && mouseY >= mouseY1 && mouseY <= mouseY2;
        if (clicked) {
            if (MODE == DISPLAY) {
                if (object instanceof IClickable) {
                    if(((IClickable)object).onClicked(stack)) return true;
                }
            } else return super.click();
        }

        return false;
    }
}
