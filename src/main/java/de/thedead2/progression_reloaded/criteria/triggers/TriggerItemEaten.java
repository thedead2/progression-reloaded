package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IMiniIcon;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static de.thedead2.progression_reloaded.ItemProgression.ItemMeta.eat;
import static de.thedead2.progression_reloaded.ItemProgression.getStackFromMeta;

@ProgressionRule(name="onEaten", color=0xFF00B285)
public class TriggerItemEaten extends TriggerBaseItemFilter implements IMiniIcon, ISpecialFieldProvider {
    private static final ItemStack mini = getStackFromMeta(eat);

    @Override
    public ITrigger copy() {
        return copyCounter(copyFilter(new TriggerItemEaten()));
    }

    @Override
    public ItemStack getMiniIcon() {
        return mini;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) fields.add(ProgressionAPI.fields.getItemPreview(this, "filters", 30, 35, 1.9F));
        else fields.add(ProgressionAPI.fields.getItemPreview(this, "filters", 65, 35, 1.9F));
    }

    @SubscribeEvent
    public void onEvent(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            ProgressionAPI.registry.fireTrigger(((EntityPlayer)event.getEntityLiving()), getProvider().getUnlocalisedName(), event.getItem());
        }
    }

    @Override
    protected boolean canIncrease(Object... data) {
        ItemStack item = (ItemStack) data[0];
        for (IFilterProvider filter : filters) {
            if (filter.getProvided().matches((ItemStack) data[0])) return true;
        }

        return false;
    }
}
