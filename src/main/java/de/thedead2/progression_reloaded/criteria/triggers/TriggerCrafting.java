package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IClickable;
import de.thedead2.progression_reloaded.api.special.IMiniIcon;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.plugins.jei.ProgressionJEI;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

import java.util.List;
import java.util.UUID;

import static de.thedead2.progression_reloaded.ItemProgression.ItemMeta.craft;
import static de.thedead2.progression_reloaded.ItemProgression.getStackFromMeta;

@Optional.Interface(iface = "special.api.de.the_dead_2.progression_reloaded.IClickable", modid = "JEI")
@ProgressionRule(name="crafting", color=0xFF663300)
public class TriggerCrafting extends TriggerBaseItemFilter implements IClickable, IMiniIcon, ISpecialFieldProvider {
    private static final ItemStack mini = getStackFromMeta(craft);

    public int timesCrafted = 1;
    protected transient int timesItemCrafted;

    @Override
    public ITrigger copy() {
        TriggerCrafting trigger = new TriggerCrafting();
        trigger.timesCrafted = timesCrafted;
        return copyCounter(copyFilter(trigger));
    }

    @Override
    public ItemStack getMiniIcon() {
        return mini;
    }

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("trigger.crafting.description", amount);
    }

    @Optional.Method(modid = "JEI")
    @Override
    public boolean onClicked(ItemStack stack) {
        try {
            ProgressionJEI.runtime.showRecipes(stack);
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItemPreview(this, "filters", 30, 35, 1.9F));
        else fields.add(ProgressionAPI.fields.getItemPreview(this, "filters", 65, 35, 1.9F));
    }

    @SubscribeEvent
    public void onEvent(ItemCraftedEvent event) {
        ItemStack result = event.crafting.copy();
        if (result.stackSize != 0) {
            ProgressionAPI.registry.fireTrigger(event.player, getProvider().getUnlocalisedName(), event.crafting.copy());
        }
    }

    @Override
    public boolean isCompleted() {
        return counter >= amount && timesItemCrafted >= timesCrafted;
    }

    @Override
    public boolean onFired(UUID uuid, Object... additional) {
        ItemStack crafted = (ItemStack) (additional[0]);
        for (IFilterProvider filter : filters) {
            if (filter.getProvided().matches(crafted)) {
                counter += crafted.stackSize;
                timesItemCrafted++;
                return true;
            }
        }

        return true;
    }

    @Override
    public void readDataFromNBT(NBTTagCompound tag) {
        super.readDataFromNBT(tag);
        timesItemCrafted = tag.getInteger("Times");
    }

    @Override
    public void writeDataToNBT(NBTTagCompound tag) {
        super.writeDataToNBT(tag);
        tag.setInteger("Times", timesItemCrafted);
    }
}
