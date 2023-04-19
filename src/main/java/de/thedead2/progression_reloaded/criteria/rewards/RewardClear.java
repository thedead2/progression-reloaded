package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.ItemProgression;
import de.thedead2.progression_reloaded.ItemProgression.ItemMeta;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.gui.core.GuiList;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterFieldPreview;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;

@ProgressionRule(name="clear", color=0xFF69008C, meta="clearInventory")
public class RewardClear extends RewardBaseItemFilter implements ICustomDisplayName, ICustomDescription, ICustomWidth, ICustomTooltip, ISpecialFieldProvider {
    public int stackSize = 1;

    public RewardClear() {
        BROKEN = ItemProgression.getStackFromMeta(ItemMeta.clearInventory);
    }

    @Override
    public String getDisplayName() {
        return GuiList.MODE == EDIT ? de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName()) : de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".display");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemStack getIcon() {
        super.getIcon(); //Update the preview
        return BROKEN;
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == EDIT ? 100 : 60;
    }

    @Override
    public void addTooltip(List list) {
        list.add(TextFormatting.RED + "Remove Item");
        ItemStack stack = preview == null ? BROKEN : preview;
        list.add(stack.getDisplayName() + " x" + stack.stackSize);
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == EDIT) fields.add(new ItemFilterFieldPreview("filters", this, 25, 30, 2.8F));
        else fields.add(new ItemFilterFieldPreview("filters", this, 5, 25, 2.8F));
    }

    @Override
    public void reward(EntityPlayerMP player) {
        int taken = 0;
        for (int i = 0; i < player.inventory.mainInventory.length && taken < stackSize; i++) {
            ItemStack check = player.inventory.mainInventory[i];
            int decrease = 0;

            if (check != null) {
                for (int j = 0; j < check.stackSize && taken < stackSize; j++) {
                    for (IFilterProvider filter : filters) {
                        if (filter.getProvided().matches(check)) {
                            decrease++;
                            taken++;
                        }
                    }
                }

                if (decrease > 0) player.inventory.decrStackSize(i, decrease);
            }
        }
    }
}
